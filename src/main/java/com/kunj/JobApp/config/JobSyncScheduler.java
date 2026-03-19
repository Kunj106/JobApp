package com.kunj.JobApp.config;

import com.kunj.JobApp.service.JobScraperService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class JobSyncScheduler
{
    private final JobScraperService jobScraperService;

    @Value("${job.sync.enabled:true}")
    private boolean syncEnabled;

    private LocalDateTime lastSyncAt;
    private int           lastSyncAdded;
    private String        lastSyncStatus = "Never synced";

    // Daily at 1:00 AM
    @Scheduled(cron = "0 0 1 * * *")
    public void scheduledSync() {
        if (!syncEnabled) { log.info("Job sync disabled via config."); return; }
        log.info("⏰  Scheduled job sync starting (1:00 AM)...");
        runSync();
    }

    // ── Once 5 minutes after startup ─────────────────────────────────────────
    @Scheduled(initialDelay = 300_00, fixedDelay = Long.MAX_VALUE)
    public void startupSync() {
        if (!syncEnabled) return;
        log.info("🚀  Startup job sync starting (30s after boot)...");
        runSync();
    }

    // ── Manual trigger from UI ────────────────────────────────────────────────
    public SyncResult manualSync() {
        log.info("🖱️  Manual job sync triggered from UI...");
        return runSync();
    }

    private SyncResult runSync() {
        long start = System.currentTimeMillis();
        try {
            int added = jobScraperService.syncJobs();
            lastSyncAt     = LocalDateTime.now();
            lastSyncAdded  = added;
            lastSyncStatus = "SUCCESS";
            long elapsed   = (System.currentTimeMillis() - start) / 1000;
            log.info("✅  Job sync done in {}s — {} new jobs added.", elapsed, added);
            return new SyncResult(added, "SUCCESS",
                    "Synced " + added + " new jobs in " + elapsed + "s");
        } catch (Exception e) {
            lastSyncStatus = "FAILED: " + e.getMessage();
            log.error("❌  Job sync failed: {}", e.getMessage());
            return new SyncResult(0, "FAILED", e.getMessage());
        }
    }

    // ── Getters for status endpoint ───────────────────────────────────────────
    public LocalDateTime getLastSyncAt()     { return lastSyncAt; }
    public int           getLastSyncAdded()  { return lastSyncAdded; }
    public String        getLastSyncStatus() { return lastSyncStatus; }

    public record SyncResult(int added, String status, String message) {}
}
