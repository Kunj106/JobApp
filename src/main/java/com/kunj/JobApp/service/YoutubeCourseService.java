package com.kunj.JobApp.service;

import com.kunj.JobApp.config.CourseSyncScheduler;
import com.kunj.JobApp.entity.CourseCategory;
import com.kunj.JobApp.entity.FreeCourse;
import com.kunj.JobApp.repository.FreeCourseRepository;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

@Service
@RequiredArgsConstructor
public class YoutubeCourseService
{
    private static final Logger log =
            LoggerFactory.getLogger(CourseSyncScheduler.class);

    private final FreeCourseRepository courseRepository;
    private final ObjectMapper objectMapper;

    // Browser-like headers to avoid bot detection
    private static final Map<String, String> HEADERS = Map.of(
            "User-Agent",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
                    "(KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36",
            "Accept-Language", "en-US,en;q=0.9",
            "Accept", "text/html,application/xhtml+xml,*/*;q=0.8"
    );

    // ── YouTube search queries ────────────────────────────────────────────────
    private static final List<ScrapeTarget> YT_TARGETS = List.of(
            new ScrapeTarget("Java tutorial full course free 2024",
                    "Core Java",     CourseCategory.CORE_JAVA,     "Beginner"),
            new ScrapeTarget("Spring Boot complete tutorial free 2024",
                    "Spring Boot",   CourseCategory.SPRING_BOOT,   "Intermediate"),
            new ScrapeTarget("Java DSA data structures full course free",
                    "DSA",           CourseCategory.DSA,           "Intermediate"),
            new ScrapeTarget("MySQL complete tutorial beginner free 2024",
                    "SQL",           CourseCategory.SQL_DATABASE,  "Beginner"),
            new ScrapeTarget("AWS cloud practitioner full course free 2024",
                    "AWS",           CourseCategory.AWS_CLOUD,     "Beginner"),
            new ScrapeTarget("Git GitHub complete tutorial free 2024",
                    "Git & GitHub",  CourseCategory.GIT_GITHUB,    "Beginner"),
            new ScrapeTarget("Java microservices Spring Boot complete free",
                    "Microservices", CourseCategory.MICROSERVICES, "Advanced"),
            new ScrapeTarget("Java streams lambda functional programming full course",
                    "Advanced Java", CourseCategory.ADVANCED_JAVA, "Intermediate"),
            new ScrapeTarget("Spring AI Java generative AI complete tutorial",
                    "Spring AI",     CourseCategory.SPRING_AI,     "Advanced"),
            new ScrapeTarget("Java system design complete course interview",
                    "System Design", CourseCategory.SYSTEM_DESIGN, "Advanced")
    );

    // ── Udemy free search queries ─────────────────────────────────────────────
    private static final List<ScrapeTarget> UDEMY_TARGETS = List.of(
            new ScrapeTarget("java programming",
                    "Core Java",     CourseCategory.CORE_JAVA,     "Beginner"),
            new ScrapeTarget("spring boot",
                    "Spring Boot",   CourseCategory.SPRING_BOOT,   "Intermediate"),
            new ScrapeTarget("mysql sql database",
                    "SQL",           CourseCategory.SQL_DATABASE,  "Beginner"),
            new ScrapeTarget("aws cloud",
                    "AWS",           CourseCategory.AWS_CLOUD,     "Beginner"),
            new ScrapeTarget("git github",
                    "Git & GitHub",  CourseCategory.GIT_GITHUB,    "Beginner")
    );

    // ─────────────────────────────────────────────────────────────────────────
    //  MAIN ENTRY — called by CourseSyncScheduler
    // ─────────────────────────────────────────────────────────────────────────

    public int syncCourses() {
        log.info("🕷️  Starting no-API scrape: YouTube + Udemy + GitHub...");
        int total = 0;
        total += scrapeYouTube();
        total += scrapeFreeCodeCamp();
        total += scrapeGitHub();
        log.info("✅ Scrape complete — {} new courses added total.", total);
        return total;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  SOURCE 1 — YOUTUBE (extracts ytInitialData JSON — no API key)
    // ─────────────────────────────────────────────────────────────────────────

    private int scrapeYouTube() {
        int added = 0;
        for (ScrapeTarget t : YT_TARGETS) {
            try {
                List<FreeCourse> courses = fetchYouTubeSearch(t);
                added += saveNew(courses);
                sleep(1500, 2500); // polite delay between requests
            } catch (Exception e) {
                log.warn("YouTube scrape failed [{}]: {}", t.query(), e.getMessage());
            }
        }
        log.info("  YouTube → {} new courses", added);
        return added;
    }

    private List<FreeCourse> fetchYouTubeSearch(ScrapeTarget target) throws Exception {
        // sp=EgIQAQ%3D%3D filters for "long" videos (>20 min) — actual courses not shorts
        String q   = java.net.URLEncoder.encode(target.query() + " full course", "UTF-8");
        String url = "https://www.youtube.com/results?search_query=" + q + "&sp=EgIQAQ%3D%3D";

        String html = Jsoup.connect(url)
                .headers(HEADERS)
                .ignoreContentType(true)
                .timeout(20_000)
                .execute().body();

        // YouTube embeds ALL search data as: var ytInitialData = { ... };
        // We extract that JSON blob without any API key
        int start = html.indexOf("var ytInitialData = ");
        if (start == -1) { log.debug("ytInitialData not found"); return List.of(); }
        start += "var ytInitialData = ".length();

        // Walk chars to find the balanced closing brace
        int depth = 0, end = start;
        for (; end < html.length(); end++) {
            char c = html.charAt(end);
            if      (c == '{') depth++;
            else if (c == '}') { if (--depth == 0) { end++; break; } }
        }

        JsonNode root = objectMapper.readTree(html.substring(start, end));

        // Navigate the deeply nested structure to reach video renderers
        JsonNode sections = root
                .path("contents")
                .path("twoColumnSearchResultsRenderer")
                .path("primaryContents")
                .path("sectionListRenderer")
                .path("contents");

        List<FreeCourse> results = new ArrayList<>();
        for (JsonNode section : sections) {
            for (JsonNode item : section.path("itemSectionRenderer").path("contents")) {
                JsonNode vr = item.path("videoRenderer");
                if (vr.isMissingNode()) continue;
                FreeCourse c = mapYouTubeVideo(vr, target);
                if (c != null) results.add(c);
                if (results.size() >= 5) return results;
            }
        }
        return results;
    }

    private FreeCourse mapYouTubeVideo(JsonNode vr, ScrapeTarget t) {
        try {
            String videoId = vr.path("videoId").asText();
            if (videoId.isBlank()) return null;

            String title = vr.path("title").path("runs").path(0).path("text").asText("").trim();
            if (title.isBlank()) return null;

            // Must be a long video — skip shorts and clip compilations
            String durRaw = vr.path("lengthText").path("simpleText").asText("");
            if (!isLongVideo(durRaw)) return null;
            if (!isLikelyCourse(title)) return null;

            String channel  = vr.path("ownerText").path("runs").path(0).path("text").asText("Unknown");
            String snippet  = vr.path("descriptionSnippet").path("runs").path(0).path("text").asText("");
            String thumb    = vr.path("thumbnail").path("thumbnails").path(1).path("url")
                    .asText(vr.path("thumbnail").path("thumbnails").path(0).path("url").asText(""));

            return FreeCourse.builder()
                    .title(cleanTitle(title))
                    .platform("YouTube")
                    .technology(t.technology())
                    .description(snippet.length() > 400 ? snippet.substring(0, 397) + "…" : snippet)
                    .instructor(channel)
                    .duration(formatDuration(durRaw))
                    .level(t.level())
                    .courseLink("https://www.youtube.com/watch?v=" + videoId)
                    .thumbnailUrl(thumb.startsWith("http") ? thumb : null)
                    .category(t.category())
                    .sourceId("yt_" + videoId)
                    .sourceType("YOUTUBE")
                    .fetchedAt(LocalDateTime.now())
                    .isAutoFetched(true)
                    .build();
        } catch (Exception e) { return null; }
    }

    // ── UDEMY_TARGETS removed — replace with freeCodeCamp ────────────────────

    private int scrapeFreeCodeCamp() {
        int added = 0;
        try {
            // freeCodeCamp curriculum JSON — fully public, no auth needed
            String url = "https://raw.githubusercontent.com/freeCodeCamp/freeCodeCamp/" +
                    "main/shared/config/curriculum.json";
            String body = Jsoup.connect(url)
                    .ignoreContentType(true).timeout(15_000).execute().body();

            JsonNode root = objectMapper.readTree(body);

            // Map freeCodeCamp cert names → our categories
            Map<String, CourseCategory> certMap = Map.of(
                    "JavaScript Algorithms", CourseCategory.CORE_JAVA,
                    "Back End Development", CourseCategory.SPRING_BOOT,
                    "Relational Database", CourseCategory.SQL_DATABASE,
                    "APIs and Microservices", CourseCategory.MICROSERVICES
            );

            for (Map.Entry<String,CourseCategory> entry : certMap.entrySet()) {
                String sid = "fcc_" + entry.getKey().replaceAll("[^a-zA-Z0-9]", "_");
                if (courseRepository.existsBySourceId(sid)) continue;

                courseRepository.save(FreeCourse.builder()
                        .title("freeCodeCamp: " + entry.getKey() + " Certification")
                        .platform("freeCodeCamp")
                        .technology(entry.getValue().name())
                        .description("Free certification course from freeCodeCamp — fully self-paced, no login required.")
                        .instructor("freeCodeCamp")
                        .level("Beginner")
                        .courseLink("https://www.freecodecamp.org/learn")
                        .category(entry.getValue())
                        .sourceId(sid)
                        .sourceType("FREECODECAMP")
                        .fetchedAt(LocalDateTime.now())
                        .isAutoFetched(true)
                        .build());
                added++;
            }
        } catch (Exception e) {
            log.warn("freeCodeCamp scrape failed: {}", e.getMessage());
        }
        return added;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  SOURCE 3 — GITHUB AWESOME-JAVA (raw README.md — no rate limit)
    // ─────────────────────────────────────────────────────────────────────────

    private int scrapeGitHub() {
        int added = 0;
        try {
            String[] urls = {
                    "https://raw.githubusercontent.com/akullpp/awesome-java/master/docs/awesome-java.md",
                    "https://raw.githubusercontent.com/akullpp/awesome-java/master/README.md"
            };
            for (String url : urls) {
                try {
                    String md = Jsoup.connect(url).headers(HEADERS)
                            .ignoreContentType(true).timeout(15_000).execute().body();
                    if (md != null && md.length() > 1000) {
                        added = parseAwesomeJava(md);
                        break;
                    }
                } catch (Exception e) { log.debug("GitHub URL {} failed: {}", url, e.getMessage()); }
            }
        } catch (Exception e) {
            log.warn("GitHub scrape failed: {}", e.getMessage());
        }
        log.info("  GitHub → {} new entries", added);
        return added;
    }

    private int parseAwesomeJava(String md) {
        // Map markdown section keywords → our categories + technology names
        record Section(String keyword, String tech, CourseCategory cat) {}
        List<Section> sectionMap = List.of(
                new Section("frameworks",          "Spring Boot",   CourseCategory.SPRING_BOOT),
                new Section("web frameworks",      "Spring Boot",   CourseCategory.SPRING_BOOT),
                new Section("persistence",         "SQL",           CourseCategory.SQL_DATABASE),
                new Section("database",            "SQL",           CourseCategory.SQL_DATABASE),
                new Section("orm",                 "SQL",           CourseCategory.SQL_DATABASE),
                new Section("microservices",       "Microservices", CourseCategory.MICROSERVICES),
                new Section("messaging",           "Microservices", CourseCategory.MICROSERVICES),
                new Section("cloud",               "AWS",           CourseCategory.AWS_CLOUD),
                new Section("testing",             "Advanced Java", CourseCategory.ADVANCED_JAVA),
                new Section("security",            "Advanced Java", CourseCategory.ADVANCED_JAVA),
                new Section("functional",          "Advanced Java", CourseCategory.ADVANCED_JAVA),
                new Section("build",               "Core Java",     CourseCategory.CORE_JAVA),
                new Section("code analysis",       "Core Java",     CourseCategory.CORE_JAVA),
                new Section("machine learning",    "Spring AI",     CourseCategory.SPRING_AI),
                new Section("artificial intelligence","Spring AI",  CourseCategory.SPRING_AI)
        );

        Pattern linkPat = Pattern.compile("^- \\[([^\\]]+)]\\(([^)]+)\\)\\s*[-–]?\\s*(.*)$");

        String  curTech = null;
        CourseCategory curCat = null;
        int saved = 0, perSection = 0;

        for (String line : md.split("\n")) {
            line = line.trim();

            if (line.startsWith("## ") || line.startsWith("### ")) {
                String hdr = line.replaceAll("^#{2,3}\\s+", "").toLowerCase();
                curTech = null; curCat = null; perSection = 0;
                for (Section s : sectionMap) {
                    if (hdr.contains(s.keyword())) {
                        curTech = s.tech(); curCat = s.cat(); break;
                    }
                }
                continue;
            }

            if (curTech == null || perSection >= 4) continue;

            Matcher m = linkPat.matcher(line);
            if (!m.matches()) continue;

            String name = m.group(1).trim();
            String url  = m.group(2).trim();
            String desc = m.group(3).trim();

            if (!url.startsWith("http") || name.length() < 3) continue;

            String sid = "gh_" + url.replaceAll("[^a-zA-Z0-9]", "")
                    .substring(Math.max(0, url.length() - 60));
            if (courseRepository.existsBySourceId(sid)) { perSection++; continue; }

            courseRepository.save(FreeCourse.builder()
                    .title("📦  " + name)
                    .platform("GitHub")
                    .technology(curTech)
                    .description(desc.isBlank()
                            ? "Curated from awesome-java — community-maintained Java resource."
                            : desc.substring(0, Math.min(450, desc.length())))
                    .instructor("awesome-java community")
                    .level("Intermediate")
                    .courseLink(url)
                    .category(curCat)
                    .sourceId(sid)
                    .sourceType("GITHUB")
                    .fetchedAt(LocalDateTime.now())
                    .isAutoFetched(true)
                    .build());
            saved++; perSection++;
        }
        return saved;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  UTILITY
    // ─────────────────────────────────────────────────────────────────────────

    private int saveNew(List<FreeCourse> courses) {
        int n = 0;
        for (FreeCourse c : courses) {
            if (c.getSourceId() != null && !courseRepository.existsBySourceId(c.getSourceId())) {
                courseRepository.save(c);
                log.info("  ✚ [{}][{}] {}", c.getSourceType(), c.getTechnology(), c.getTitle());
                n++;
            }
        }
        return n;
    }

    /** Long video = has hours component, or 20+ minutes  */
    private boolean isLongVideo(String dur) {
        if (dur == null || dur.isBlank()) return false;
        String[] p = dur.split(":");
        if (p.length == 3) return true;
        try { return p.length == 2 && Integer.parseInt(p[0]) >= 20; }
        catch (NumberFormatException e) { return false; }
    }

    private boolean isLikelyCourse(String title) {
        String t = title.toLowerCase();
        boolean ok   = t.contains("tutorial") || t.contains("course") || t.contains("complete")
                || t.contains("full") || t.contains("learn") || t.contains("guide")
                || t.contains("masterclass") || t.contains("bootcamp") || t.contains("beginner");
        boolean junk = t.contains("shorts") || t.contains("react ") || t.contains("angular")
                || t.contains("python") || t.contains("javascript") || t.contains("salary")
                || t.contains("interview tips") || t.contains("part ");
        return ok && !junk;
    }

    /** HH:MM:SS → "4h 30m" */
    private String formatDuration(String dur) {
        if (dur == null || dur.isBlank()) return null;
        String[] p = dur.split(":");
        try {
            if (p.length == 3) {
                int h = Integer.parseInt(p[0]), m = Integer.parseInt(p[1]);
                return (h > 0 ? h + "h " : "") + m + "m";
            }
            if (p.length == 2) return Integer.parseInt(p[0]) + "m";
        } catch (NumberFormatException ignored) {}
        return dur;
    }

    private String cleanTitle(String t) {
        return t.replaceAll("(?i)\\s*\\[.*(free|course|2024|2025|full|hd).*]", "")
                .replaceAll("(?i)\\s*\\|.*$", "")
                .replaceAll("(?i)\\s*-\\s*(full|complete) (course|tutorial).*$", "")
                .replaceAll("\\(20\\d{2}\\)", "")
                .replaceAll("\\s{2,}", " ").trim();
    }

    private void sleep(int minMs, int maxMs) {
        try { Thread.sleep(minMs + (long)(Math.random() * (maxMs - minMs))); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    private record ScrapeTarget(
            String query, String technology,
            CourseCategory category, String level) {}
}
