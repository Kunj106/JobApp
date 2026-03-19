package com.kunj.JobApp.config;

import com.kunj.JobApp.service.YoutubeCourseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class CourseSyncScheduler
{
    private static final Logger log =
            LoggerFactory.getLogger(CourseSyncScheduler.class);

    private final YoutubeCourseService youtubeCourseService;

    public CourseSyncScheduler(YoutubeCourseService youtubeCourseService) {
        this.youtubeCourseService = youtubeCourseService;
    }

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Track last sync info for the /api/courses/sync-status endpoint
    private LocalDateTime lastSyncAt;
    private int lastSyncAdded = 0;
    private String lastSyncStatus = "Never synced";

    /**
     * Runs daily at 2:00 AM.
     * cron = "seconds minutes hours day month weekday"
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void scheduledSync() {
        log.info("⏰ Scheduled course sync starting at {}", LocalDateTime.now().format(FMT));
        runSync("SCHEDULED");
    }

    /**
     * Also runs 5 minutes after app startup — so you get fresh courses immediately on first run.
     * fixedDelay = won't run again until previous run completes.
     */
    @Scheduled(initialDelay = 300_000, fixedDelay = Long.MAX_VALUE)
    public void startupSync() {
        log.info("🚀 Startup course sync (5 min after boot) at {}", LocalDateTime.now().format(FMT));
        runSync("STARTUP");
    }

    /** Called by CourseSyncService for manual triggers from the API */
    public SyncResult manualSync() {
        log.info("👆 Manual course sync triggered at {}", LocalDateTime.now().format(FMT));
        return runSync("MANUAL");
    }

    private SyncResult runSync(String trigger) {
        try {
            long start = System.currentTimeMillis();
            int added = youtubeCourseService.syncCourses();
            long elapsed = System.currentTimeMillis() - start;

            lastSyncAt     = LocalDateTime.now();
            lastSyncAdded  = added;
            lastSyncStatus = "OK";

            log.info("✅ Course sync [{}] complete: {} new courses added in {}ms", trigger, added, elapsed);
            return new SyncResult(true, added, lastSyncAt, elapsed + "ms", null);

        } catch (Exception e) {
            lastSyncStatus = "ERROR: " + e.getMessage();
            log.error("❌ Course sync [{}] failed: {}", trigger, e.getMessage(), e);
            return new SyncResult(false, 0, LocalDateTime.now(), "0ms", e.getMessage());
        }
    }

    // ── Status getters (used by CourseController) ─────────────────────────────

    public LocalDateTime getLastSyncAt()     { return lastSyncAt; }
    public int           getLastSyncAdded()  { return lastSyncAdded; }
    public String        getLastSyncStatus() { return lastSyncStatus; }

    // ── Result record ─────────────────────────────────────────────────────────

    public record SyncResult(
            boolean success,
            int newCoursesAdded,
            LocalDateTime syncedAt,
            String duration,
            String error
    ) {}
}
