package com.kunj.JobApp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kunj.JobApp.entity.Job;
import com.kunj.JobApp.entity.JobType;
import com.kunj.JobApp.repository.JobRepository;
import com.kunj.JobApp.entity.CompanyType;
import com.kunj.JobApp.entity.Job;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Fetches live Java/tech jobs from multiple platforms daily.
 *
 * Sources & methods:
 *  1. RemoteOK       — Free public JSON API (no key needed)     ✅ Remote jobs
 *  2. HackerNews/YC  — Free Firebase JSON API (no key needed)   ✅ Startup/YC jobs
 *  3. Adzuna India   — Free REST API (free key needed)          ✅ Indian market jobs
 *  4. Unstop         — HTML scraping via Jsoup                  ⚠️  Competitions + jobs
 *  5. Internshala    — HTML scraping via Jsoup                  ⚠️  Internships + fresher
 *  6. Wellfound      — HTML scraping via Jsoup                  ⚠️  Startup jobs
 *
 * All fetched jobs are deduped via sourceId before saving.
 * Runs daily at 1:00 AM via JobSyncScheduler.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class JobScraperService {

    private final JobRepository jobRepository;
    private final ObjectMapper objectMapper;

    @Value("${adzuna.app.id:}")
    private String adzunaAppId;

    @Value("${adzuna.app.key:}")
    private String adzunaAppKey;

    private final OkHttpClient http = new OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();

    private static final Map<String, String> BROWSER_HEADERS = Map.of(
            "User-Agent",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
                    "(KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36",
            "Accept-Language", "en-US,en;q=0.9",
            "Accept", "text/html,application/xhtml+xml,*/*;q=0.8"
    );

    // ─────────────────────────────────────────────────────────────────────────
    //  MAIN ENTRY — called by JobSyncScheduler
    // ─────────────────────────────────────────────────────────────────────────

    public int syncJobs() {
        log.info("🔍  Starting live job sync from all platforms...");
        int total = 0;

        total += fetchRemoteOK();
        total += fetchHackerNewsJobs();
        total += fetchAdzunaIndia();
        total += scrapeUnstop();
        total += scrapeWellfound();

        // Auto-expire jobs older than 30 days (mark isActive = false)
        expireOldJobs();

        log.info("✅  Job sync complete — {} new jobs added.", total);
        return total;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  SOURCE 1 — REMOTEOK (free JSON API, no key needed)
    //  Endpoint: https://remoteok.com/api
    //  Returns: Array of remote tech jobs updated in real-time
    // ─────────────────────────────────────────────────────────────────────────

    private int fetchRemoteOK() {
        int added = 0;
        try {
            // Java/Spring/Backend Java remote jobs
            String[] tags = {"java", "spring", "backend", "kotlin"};
            for (String tag : tags) {
                String url = "https://remoteok.com/api?tag=" + tag;
                Request req = new Request.Builder()
                        .url(url)
                        .header("User-Agent", "Mozilla/5.0")
                        .build();

                try (Response resp = http.newCall(req).execute()) {
                    if (!resp.isSuccessful()) continue;
                    JsonNode root = objectMapper.readTree(resp.body().string());

                    for (JsonNode item : root) {
                        // First element is metadata, skip it
                        if (!item.has("id") || !item.has("company")) continue;

                        String sid = "remoteok_" + item.path("id").asText();
                        if (jobRepository.existsBySourceId(sid)) continue;

                        String title    = item.path("position").asText("").trim();
                        String company  = item.path("company").asText("Unknown");
                        String desc     = cleanHtml(item.path("description").asText(""));
                        String applyUrl = item.path("url").asText("");
                        String salary   = formatSalary(
                                item.path("salary_min").asLong(0),
                                item.path("salary_max").asLong(0));
                        String tagsStr  = item.path("tags").toString()
                                .replaceAll("[\\[\\]\"]", "").replace(",", ", ");

                        if (title.isBlank() || applyUrl.isBlank()) continue;
                        if (!isJavaRelated(title + " " + tagsStr)) continue;

                        Job job = Job.builder()
                                .title(title)
                                .company(company)
                                .location("Remote / Worldwide")
                                .jobType(JobType.JOB)
                                .companyType(guessCompanyType(company, desc))
                                .description(desc.length() > 800 ? desc.substring(0, 797) + "…" : desc)
                                .requirements(extractRequirements(desc))
                                .salary(salary)
                                .applyLink(applyUrl)
                                .experience(extractExperience(title + " " + desc))
                                .skills(tagsStr.isEmpty() ? tag : tagsStr)
                                .sourceId(sid)
                                .sourcePlatform("REMOTEOK")
                                .isLive(true)
                                .isRemote(true)
                                .isFresher(isFresherRole(title + " " + desc))
                                .build();

                        jobRepository.save(job);
                        added++;
                        log.info("  ✚ [RemoteOK] {}", title);
                    }
                    sleep(1000, 2000);
                } catch (Exception e) {
                    log.warn("RemoteOK tag '{}' failed: {}", tag, e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("RemoteOK fetch error: {}", e.getMessage());
        }
        log.info("  RemoteOK → {} new jobs", added);
        return added;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  SOURCE 2 — HACKER NEWS / Y COMBINATOR JOBS
    //  Uses HN Firebase API — free, no key, reliable
    //  Fetches "Who is Hiring?" monthly threads + YC job board
    // ─────────────────────────────────────────────────────────────────────────

    private int fetchHackerNewsJobs() {
        int added = 0;
        try {
            // Fetch top 200 story IDs from HN jobs section
            String idsUrl = "https://hacker-news.firebaseio.com/v0/jobstories.json";
            Request req = new Request.Builder().url(idsUrl)
                    .header("User-Agent", "Mozilla/5.0").build();

            try (Response resp = http.newCall(req).execute()) {
                if (!resp.isSuccessful()) return 0;
                JsonNode ids = objectMapper.readTree(resp.body().string());

                int count = 0;
                for (JsonNode idNode : ids) {
                    if (count >= 30) break; // fetch max 30 jobs per sync
                    long storyId = idNode.asLong();
                    String sid   = "hn_" + storyId;
                    if (jobRepository.existsBySourceId(sid)) { count++; continue; }

                    String itemUrl = "https://hacker-news.firebaseio.com/v0/item/"
                            + storyId + ".json";
                    Request itemReq = new Request.Builder().url(itemUrl)
                            .header("User-Agent", "Mozilla/5.0").build();

                    try (Response itemResp = http.newCall(itemReq).execute()) {
                        if (!itemResp.isSuccessful()) continue;
                        JsonNode item = objectMapper.readTree(itemResp.body().string());

                        String title = item.path("title").asText("").trim();
                        String text  = cleanHtml(item.path("text").asText(""));
                        String url   = item.path("url").asText("");

                        if (title.isBlank()) continue;
                        if (!isJavaRelated(title + " " + text)) {
                            count++;
                            continue; // skip non-Java jobs
                        }

                        // Parse company from HN title format: "Company (YC S24) is hiring..."
                        String company = parseHNCompany(title);
                        String applyLink = url.isBlank()
                                ? "https://news.ycombinator.com/item?id=" + storyId
                                : url;

                        Job job = Job.builder()
                                .title(parseHNJobTitle(title))
                                .company(company)
                                .location("Remote / Global")
                                .jobType(JobType.JOB)
                                .companyType(CompanyType.STARTUP)
                                .description(text.length() > 800 ? text.substring(0, 797) + "…" : text)
                                .requirements(extractRequirements(text))
                                .applyLink(applyLink)
                                .experience(extractExperience(title + " " + text))
                                .skills(extractSkills(text))
                                .sourceId(sid)
                                .sourcePlatform("YC_JOBS")
                                .isLive(true)
                                .isRemote(true)
                                .isFresher(isFresherRole(title + " " + text))
                                .build();

                        jobRepository.save(job);
                        added++;
                        log.info("  ✚ [YC/HN] {}", title);
                    }
                    count++;
                    sleep(300, 600); // light throttle — Firebase is rate-limited
                }
            }
        } catch (Exception e) {
            log.error("HackerNews Jobs fetch error: {}", e.getMessage());
        }
        log.info("  YC/HN Jobs → {} new jobs", added);
        return added;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  SOURCE 3 — ADZUNA INDIA (free API, covers Indian job market)
    //  Free key: https://developer.adzuna.com (takes 2 minutes, no credit card)
    //  Covers: Bangalore, Mumbai, Hyderabad, Pune, Chennai, Delhi
    //  Returns: Java, Spring Boot, Backend jobs in India
    // ─────────────────────────────────────────────────────────────────────────

    private int fetchAdzunaIndia() {
        if (adzunaAppId.isBlank() || adzunaAppKey.isBlank()
                || adzunaAppId.equals("your_adzuna_app_id")) {
            log.info("  Adzuna: API key not configured — skipping (optional)");
            return 0;
        }

        int added = 0;
        String[] queries   = {"java developer", "spring boot", "java fresher", "backend developer java"};
        String[] locations = {"bangalore", "mumbai", "hyderabad", "pune"};

        for (String q : queries) {
            for (String loc : locations) {
                try {
                    String url = "https://api.adzuna.com/v1/api/jobs/in/search/1" +
                            "?app_id="     + adzunaAppId +
                            "&app_key="    + adzunaAppKey +
                            "&results_per_page=10" +
                            "&what="       + java.net.URLEncoder.encode(q, "UTF-8") +
                            "&where="      + java.net.URLEncoder.encode(loc, "UTF-8") +
                            "&max_days_old=1" +  // only last 24 hours
                            "&content-type=application/json";

                    Request req = new Request.Builder()
                            .url(url)
                            .header("User-Agent", "Mozilla/5.0").build();

                    try (Response resp = http.newCall(req).execute()) {
                        if (!resp.isSuccessful()) continue;
                        JsonNode root = objectMapper.readTree(resp.body().string());

                        for (JsonNode item : root.path("results")) {
                            String sid = "adzuna_" + item.path("id").asText();
                            if (jobRepository.existsBySourceId(sid)) continue;

                            String title     = item.path("title").asText("").trim();
                            String company   = item.path("company").path("display_name").asText("Unknown");
                            String desc      = cleanHtml(item.path("description").asText(""));
                            String applyLink = item.path("redirect_url").asText("");
                            String salaryMin = item.path("salary_min").asText("");
                            String salaryMax = item.path("salary_max").asText("");
                            String salary    = (!salaryMin.isBlank() && !salaryMax.isBlank())
                                    ? "₹" + formatIndianSalary(salaryMin) + " – ₹" + formatIndianSalary(salaryMax)
                                    : null;

                            if (title.isBlank() || applyLink.isBlank()) continue;

                            Job job = Job.builder()
                                    .title(title)
                                    .company(company)
                                    .location(capitalize(loc) + ", India")
                                    .jobType(isInternship(title) ? JobType.INTERNSHIP : JobType.JOB)
                                    .companyType(guessCompanyType(company, desc))
                                    .description(desc.length() > 800 ? desc.substring(0, 797) + "…" : desc)
                                    .requirements(extractRequirements(desc))
                                    .salary(salary)
                                    .applyLink(applyLink)
                                    .experience(extractExperience(title + " " + desc))
                                    .skills(extractSkills(desc))
                                    .sourceId(sid)
                                    .sourcePlatform("ADZUNA")
                                    .isLive(true)
                                    .isRemote(isRemoteJob(title + " " + desc))
                                    .isFresher(isFresherRole(title + " " + desc))
                                    .build();

                            jobRepository.save(job);
                            added++;
                            log.info("  ✚ [Adzuna] {} @ {}", title, loc);
                        }
                    }
                    sleep(500, 1000);
                } catch (Exception e) {
                    log.warn("Adzuna '{}'/'{}' failed: {}", q, loc, e.getMessage());
                }
            }
        }
        log.info("  Adzuna → {} new jobs", added);
        return added;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  SOURCE 4 — UNSTOP (scraping — competitions + fresher jobs)
    //  URL: https://unstop.com/jobs?oppurtunity=jobs&per_page=20
    // ─────────────────────────────────────────────────────────────────────────

    private int scrapeUnstop() {
        int added = 0;
        try {
            String url = "https://unstop.com/api/public/opportunity/search-result" +
                    "?opportunity=jobs&per_page=20&orderBy=RELEVANCE&isFiltered=0" +
                    "&searchTerm=java+developer";

            Request req = new Request.Builder().url(url)
                    .headers(okhttp3.Headers.of(
                            "User-Agent", BROWSER_HEADERS.get("User-Agent"),
                            "Accept", "application/json",
                            "Referer", "https://unstop.com/jobs"
                    )).build();

            try (Response resp = http.newCall(req).execute()) {
                if (!resp.isSuccessful()) {
                    log.debug("Unstop API returned {}", resp.code());
                    return scrapeUnstopHTML(); // fallback to HTML scraping
                }
                String body = resp.body().string();
                JsonNode root = objectMapper.readTree(body);
                JsonNode items = root.path("data").path("data");

                for (JsonNode item : items) {
                    String sid = "unstop_" + item.path("id").asText();
                    if (jobRepository.existsBySourceId(sid)) continue;

                    String title   = item.path("title").asText("").trim();
                    String company = item.path("organisation").path("name").asText("Unknown");
                    String desc    = cleanHtml(item.path("description").asText(""));
                    String slug    = item.path("seo_url").asText("");
                    String applyLink = slug.isBlank()
                            ? "https://unstop.com/jobs"
                            : "https://unstop.com/" + slug;
                    String salary  = item.path("salary").asText("");
                    String exp     = item.path("experience").asText("");

                    if (title.isBlank()) continue;

                    Job job = Job.builder()
                            .title(title)
                            .company(company)
                            .location("India")
                            .jobType(isInternship(title) ? JobType.INTERNSHIP : JobType.JOB)
                            .companyType(guessCompanyType(company, desc))
                            .description(desc.length() > 800 ? desc.substring(0, 797) + "…" : desc)
                            .salary(salary.isBlank() ? null : salary)
                            .applyLink(applyLink)
                            .experience(exp.isBlank() ? extractExperience(desc) : exp)
                            .skills(extractSkills(desc))
                            .sourceId(sid)
                            .sourcePlatform("UNSTOP")
                            .isLive(true)
                            .isRemote(isRemoteJob(title + " " + desc))
                            .isFresher(true) // Unstop is mostly fresher-focused
                            .build();

                    jobRepository.save(job);
                    added++;
                    log.info("  ✚ [Unstop] {}", title);
                }
            }
        } catch (Exception e) {
            log.warn("Unstop scrape failed: {}", e.getMessage());
        }
        log.info("  Unstop → {} new jobs", added);
        return added;
    }

    /** HTML fallback for Unstop if JSON API fails */
    private int scrapeUnstopHTML() {
        int added = 0;
        try {
            Document doc = Jsoup.connect("https://unstop.com/jobs?oppurtunity=jobs&searchTerm=java")
                    .headers(BROWSER_HEADERS).timeout(20_000).get();

            Elements cards = doc.select(".job__top-left, [class*=opportunity-card], [class*=job-card]");
            for (Element card : cards) {
                String title   = card.select("h2, h3, .title, [class*=title]").text().trim();
                String company = card.select(".company, [class*=company], .organisation").text().trim();
                String href    = card.select("a").attr("href");
                if (title.isBlank() || href.isBlank()) continue;
                if (!isJavaRelated(title) || isFrontend(title)) continue;

                String link = href.startsWith("http") ? href : "https://unstop.com" + href;
                String sid  = "unstop_html_" + href.replaceAll("[^a-zA-Z0-9]", "").substring(0,
                        Math.min(40, href.length()));
                if (jobRepository.existsBySourceId(sid)) continue;

                jobRepository.save(Job.builder()
                        .title(title)
                        .company(company.isBlank() ? "Company on Unstop" : company)
                        .location("India")
                        .jobType(isInternship(title) ? JobType.INTERNSHIP : JobType.JOB)
                        .companyType(CompanyType.STARTUP)
                        .description("Java developer opportunity on Unstop.")
                        .applyLink(link)
                        .skills("Java")
                        .sourceId(sid)
                        .sourcePlatform("UNSTOP")
                        .isLive(true).isRemote(false).isFresher(true)
                        .build());
                added++;
            }
        } catch (Exception e) {
            log.debug("Unstop HTML fallback failed: {}", e.getMessage());
        }
        return added;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  SOURCE 5 — WELLFOUND / ANGELLIST (startup jobs)
    //  Scrapes the public job search page for Java startup roles
    // ─────────────────────────────────────────────────────────────────────────

    private int scrapeWellfound() {
        int added = 0;
        try {
            Document doc = Jsoup.connect("https://wellfound.com/jobs?role=software-engineer&skills=java")
                    .headers(BROWSER_HEADERS).timeout(25_000).get();

            // Wellfound renders jobs as React components — try to get embedded JSON
            Elements scripts = doc.select("script[type=application/json], script#__NEXT_DATA__");
            for (Element script : scripts) {
                try {
                    JsonNode data = objectMapper.readTree(script.html());
                    // Navigate to job listings in __NEXT_DATA__
                    JsonNode jobs = data.path("props").path("pageProps").path("jobs");
                    if (jobs.isMissingNode()) jobs = data.path("jobs");

                    for (JsonNode item : jobs) {
                        String sid     = "wellfound_" + item.path("id").asText();
                        if (jobRepository.existsBySourceId(sid)) continue;

                        String title   = item.path("title").asText("").trim();
                        String company = item.path("startup").path("name").asText(
                                item.path("company").asText("Unknown"));
                        String desc    = cleanHtml(item.path("description").asText(""));
                        String applyLink = "https://wellfound.com/jobs/" + item.path("id").asText();
                        String loc     = item.path("locationNames").path(0).asText("Remote");
                        String salMin  = item.path("compensation").path("min").asText("");
                        String salMax  = item.path("compensation").path("max").asText("");
                        String salary  = (!salMin.isBlank() && !salMax.isBlank())
                                ? "$" + salMin + "K – $" + salMax + "K" : null;

                        if (title.isBlank()) continue;

                        jobRepository.save(Job.builder()
                                .title(title)
                                .company(company)
                                .location(loc)
                                .jobType(JobType.JOB)
                                .companyType(CompanyType.STARTUP)
                                .description(desc.length() > 800 ? desc.substring(0, 797) + "…" : desc)
                                .requirements(extractRequirements(desc))
                                .salary(salary)
                                .applyLink(applyLink)
                                .experience(extractExperience(title + " " + desc))
                                .skills(extractSkills(desc))
                                .sourceId(sid)
                                .sourcePlatform("WELLFOUND")
                                .isLive(true)
                                .isRemote(loc.toLowerCase().contains("remote"))
                                .isFresher(isFresherRole(title + " " + desc))
                                .build());
                        added++;
                        log.info("  ✚ [Wellfound] {}", title);
                    }
                    if (added > 0) break; // found data, stop scanning scripts
                } catch (Exception ignored) {}
            }
        } catch (Exception e) {
            log.warn("Wellfound scrape failed: {}", e.getMessage());
        }
        log.info("  Wellfound → {} new jobs", added);
        return added;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  HOUSEKEEPING — expire old live jobs (>30 days)
    // ─────────────────────────────────────────────────────────────────────────

    private void expireOldJobs() {
        try {
            LocalDateTime cutoff = LocalDateTime.now().minusDays(30);
            List<Job> oldJobs = jobRepository.findLiveJobsSince(
                            LocalDateTime.now().minusYears(5)).stream()
                    .filter(j -> j.isLive() && j.getPostedAt().isBefore(cutoff))
                    .toList();
            oldJobs.forEach(j -> { j.setActive(false); jobRepository.save(j); });
            if (!oldJobs.isEmpty())
                log.info("  Expired {} old live jobs (>30 days)", oldJobs.size());
        } catch (Exception e) {
            log.warn("Job expiry failed: {}", e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  UTILITY HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    private boolean isJavaRelated(String text) {
        String t = text.toLowerCase();
        if (isFrontend(t)) return false;
        return t.contains("java") || t.contains("spring boot") || t.contains("spring")
                || t.contains("hibernate") || t.contains("jvm") || t.contains("jakarta")
                || t.contains("maven") || t.contains("gradle") || t.contains("microservice");
    }

    private boolean isFrontend(String text) {
        String t = text.toLowerCase();
        return t.contains("frontend") || t.contains("front-end") || t.contains("front end")
                || t.contains("react") || t.contains("angular") || t.contains("vue")
                || t.contains("next.js") || t.contains("nuxt") || t.contains("svelte")
                || t.contains("css") || t.contains("html5") || t.contains("tailwind")
                || t.contains("javascript developer") || t.contains("typescript developer")
                || t.contains("ui developer") || t.contains("ui engineer")
                || t.contains("wordpress") || t.contains("shopify");
    }

    private String truncate(String val, int max) {
        if (val == null) return null;
        return val.length() > max ? val.substring(0, max - 1) : val;
    }

    private boolean isInternship(String title) {
        String t = title.toLowerCase();
        return t.contains("intern") || t.contains("trainee") || t.contains("apprentice")
                || t.contains("graduate") || t.contains("fresher");
    }

    private boolean isFresherRole(String text) {
        String t = text.toLowerCase();
        return t.contains("fresher") || t.contains("fresh graduate") || t.contains("0-1 year")
                || t.contains("entry level") || t.contains("junior") || t.contains("trainee")
                || t.contains("0 year") || t.contains("no experience");
    }

    private boolean isRemoteJob(String text) {
        String t = text.toLowerCase();
        return t.contains("remote") || t.contains("work from home") || t.contains("wfh")
                || t.contains("anywhere") || t.contains("distributed");
    }

    private CompanyType guessCompanyType(String company, String desc) {
        String t = (company + " " + desc).toLowerCase();
        if (t.contains("bank") || t.contains("hdfc") || t.contains("icici")
                || t.contains("sbi") || t.contains("axis")) return CompanyType.BANKING;
        if (t.contains("fintech") || t.contains("payment") || t.contains("razorpay")
                || t.contains("paytm") || t.contains("zerodha") || t.contains("groww"))
            return CompanyType.FINTECH;
        if (t.contains("infosys") || t.contains("tcs") || t.contains("wipro")
                || t.contains("accenture") || t.contains("cognizant") || t.contains("ibm")
                || t.contains("microsoft") || t.contains("google") || t.contains("amazon"))
            return CompanyType.MNC;
        if (t.contains("startup") || t.contains("series a") || t.contains("series b")
                || t.contains("seed") || t.contains("yc") || t.contains("y combinator"))
            return CompanyType.STARTUP;
        return CompanyType.MIDSIZE;
    }

    private String extractExperience(String text) {
        // Regex: "2-4 years", "3+ years", "fresher", etc.
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("(?i)(fresher|0[-–]?1|\\d+[-–+]\\d*)\\s*(year|yr)")
                .matcher(text.toLowerCase());
        if (m.find()) return m.group(0).trim();
        if (text.toLowerCase().contains("fresher")) return "Fresher";
        if (text.toLowerCase().contains("entry level")) return "Entry Level";
        return null;
    }

    private String extractRequirements(String desc) {
        if (desc == null || desc.isBlank()) return null;
        // Take first 120 chars of description as requirements snippet
        String clean = desc.replaceAll("\\s+", " ").trim();
        return clean.length() > 120 ? clean.substring(0, 117) + "…" : clean;
    }

    private String extractSkills(String text) {
        List<String> skills = new ArrayList<>();
        String t = text.toLowerCase();
        String[] known = {"java", "spring boot", "spring", "hibernate", "jpa", "mysql",
                "postgresql", "mongodb", "redis", "kafka", "docker", "kubernetes", "aws",
                "git", "rest api", "microservices", "maven", "gradle", "junit", "react",
                "angular", "node.js", "python", "kotlin", "sql"};
        for (String s : known) {
            if (t.contains(s) && skills.size() < 6) skills.add(capitalize(s));
        }
        return skills.isEmpty() ? "Java" : String.join(", ", skills);
    }

    private String parseHNCompany(String title) {
        // Format: "Acme Corp (YC W24) is hiring..."
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("^(.+?)\\s*(\\(YC|is hiring| – | - )")
                .matcher(title);
        if (m.find()) return m.group(1).trim();
        return title.split(" ")[0];
    }

    private String parseHNJobTitle(String title) {
        // "Company is hiring Senior Java Engineer" → "Senior Java Engineer"
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("(?i)(hiring|seeking|looking for)\\s+(.+)")
                .matcher(title);
        if (m.find()) return m.group(2).trim();
        return title.length() > 80 ? title.substring(0, 77) + "…" : title;
    }

    private String formatSalary(long min, long max) {
        if (min == 0 && max == 0) return null;
        if (min == 0) return "$" + max / 1000 + "K";
        if (max == 0) return "$" + min / 1000 + "K+";
        return "$" + min / 1000 + "K – $" + max / 1000 + "K";
    }

    private String formatIndianSalary(String val) {
        try {
            double v = Double.parseDouble(val);
            if (v >= 100_000) return String.format("%.1fL", v / 100_000);
            return String.format("%.0f", v);
        } catch (NumberFormatException e) { return val; }
    }

    private String capitalize(String s) {
        if (s == null || s.isBlank()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private String cleanHtml(String html) {
        return Jsoup.parse(html).text().replaceAll("\\s+", " ").trim();
    }

    private void sleep(int minMs, int maxMs) {
        try { Thread.sleep(minMs + (long)(Math.random() * (maxMs - minMs))); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}