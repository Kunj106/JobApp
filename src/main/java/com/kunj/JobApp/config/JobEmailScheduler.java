package com.kunj.JobApp.config;

import com.kunj.JobApp.service.JobEmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class JobEmailScheduler
{
    private final JobEmailService jobEmailService;

    @Scheduled(cron = "${email.digest.cron:0 0 6 * * *}")
    public void sendDailyDigest() {
        log.info("⏰  6:00 AM — Sending daily Java job digest...");
        jobEmailService.sendDailyDigest();
    }
}
