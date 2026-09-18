package com.kunj.JobApp.service;

import com.kunj.JobApp.dto.JobDto;
import com.kunj.JobApp.entity.Job;
import com.kunj.JobApp.repository.JobRepository;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobEmailService
{
    private final JobRepository jobRepository;
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${email.digest.to}")
    private String recipientEmail;

    @Value("${email.digest.from}")
    private String senderEmail;

    @Value("${email.digest.location:mumbai}")
    private String digestLocation;

    @Value("${email.digest.enabled:true}")
    private boolean emailEnabled;

    //  MAIN ENTRY — called by JobEmailScheduler at 6:00 AM
    public void sendDailyDigest() {
        if (!emailEnabled) {
            log.info("📧 Email digest disabled via config — skipping.");
            return;
        }

        log.info("📧 Preparing daily job digest for {}...", recipientEmail);

        // Fetch jobs from last 24 hours matching the configured location
        LocalDateTime since   = LocalDateTime.now().minusHours(24);
        String        locLower = digestLocation.toLowerCase();

        List<Job> recentJobs = jobRepository
                .findByIsActiveTrueOrderByLastRefreshedAtDescPostedAtDesc()
                .stream()
                .filter(j -> j.getPostedAt() != null && j.getPostedAt().isAfter(since))
                .filter(j -> {
                    String loc = j.getLocation() != null ? j.getLocation().toLowerCase() : "";
                    // Include jobs in the target city OR remote jobs
                    return loc.contains(locLower) || j.isRemote() || loc.contains("remote");
                })
                .collect(Collectors.toList());

        log.info("📧 Found {} new Java jobs in last 24h for location '{}'",
                recentJobs.size(), digestLocation);

        // Convert to DTOs for the template
        List<JobDto.JobResponse> jobDtos = recentJobs.stream()
                .map(JobDto.JobResponse::from)
                .collect(Collectors.toList());

        // Build and send the email
        try {
            sendHtmlEmail(jobDtos);
            log.info("✅ Daily digest sent to {} — {} jobs included",
                    recipientEmail, jobDtos.size());
        } catch (Exception e) {
            log.error("❌ Failed to send daily digest: {}", e.getMessage(), e);
        }
    }

    //  BUILD + SEND HTML EMAIL
    private void sendHtmlEmail(List<JobDto.JobResponse> jobs) throws Exception {
        // Build Thymeleaf context
        Context ctx = new Context(Locale.ENGLISH);
        ctx.setVariable("jobs",       jobs);
        ctx.setVariable("totalJobs",  jobs.size());
        ctx.setVariable("remoteJobs", jobs.stream().filter(JobDto.JobResponse::isRemote).count());
        ctx.setVariable("fresherJobs",jobs.stream().filter(JobDto.JobResponse::isFresher).count());
        ctx.setVariable("date",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy")));
        ctx.setVariable("location",
                digestLocation.substring(0, 1).toUpperCase() + digestLocation.substring(1));

        // Render HTML from Thymeleaf template
        String htmlContent = templateEngine.process("daily-job-digest", ctx);

        // Build MIME email
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(senderEmail);
        helper.setTo(recipientEmail);
        helper.setSubject(buildSubject(jobs.size()));
        helper.setText(htmlContent, true); // true = isHtml

        mailSender.send(message);
    }

    private String buildSubject(int count) {
        String loc = digestLocation.substring(0, 1).toUpperCase() + digestLocation.substring(1);
        if (count == 0) return "☕ JavaLaunch — No new Java jobs in " + loc + " today";
        if (count == 1) return "☕ JavaLaunch — 1 new Java job in " + loc + " today!";
        return String.format("☕ JavaLaunch — %d new Java jobs in %s today! 🚀", count, loc);
    }
}
