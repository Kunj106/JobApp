package com.kunj.JobApp.service;

import com.kunj.JobApp.config.CourseSyncScheduler;
import com.kunj.JobApp.dto.CourseDto;
import com.kunj.JobApp.entity.CourseCategory;
import com.kunj.JobApp.exception.ApiException;
import com.kunj.JobApp.repository.FreeCourseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseService
{
    private final FreeCourseRepository courseRepository;
    private final CourseSyncScheduler courseSyncScheduler;

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    // ── Standard course queries ───────────────────────────────────────────────

    public List<CourseDto.CourseResponse> getAllCourses() {
        // newest first: auto-fetched YouTube courses bubble up
        return courseRepository.findAllByOrderByFetchedAtDescTechnologyAsc()
                .stream().map(CourseDto.CourseResponse::from).collect(Collectors.toList());
    }

    public List<CourseDto.CourseResponse> getCoursesByCategory(String category) {
        try {
            CourseCategory cat = CourseCategory.valueOf(category.toUpperCase());
            return courseRepository.findByCategory(cat)
                    .stream().map(CourseDto.CourseResponse::from).collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            throw new ApiException("Invalid category: " + category, HttpStatus.BAD_REQUEST);
        }
    }

    public List<CourseDto.CourseResponse> searchCourses(String query) {
        return courseRepository.searchCourses(query.toLowerCase())
                .stream().map(CourseDto.CourseResponse::from).collect(Collectors.toList());
    }

    // Group by technology — newest courses within each group first
    public Map<String, List<CourseDto.CourseResponse>> getCourseGroupedByTechnology() {
        return courseRepository.findAllByOrderByFetchedAtDescTechnologyAsc()
                .stream()
                .map(CourseDto.CourseResponse::from)
                .collect(Collectors.groupingBy(CourseDto.CourseResponse::getTechnology));
    }

    public CourseDto.CourseResponse getCourseById(Long id) {
        return courseRepository.findById(id)
                .map(CourseDto.CourseResponse::from)
                .orElseThrow(() -> new ApiException("Course not found", HttpStatus.NOT_FOUND));
    }

    // ── Sync: trigger a live YouTube fetch ───────────────────────────────────

    /**
     * Manually triggers a YouTube course sync.
     * Called by POST /api/courses/sync from the frontend "Sync Now" button.
     */
    public CourseSyncScheduler.SyncResult triggerSync() {
        return courseSyncScheduler.manualSync();
    }

    /**
     * Returns sync metadata for the frontend status banner.
     */
    public CourseDto.SyncStatusResponse getSyncStatus() {
        CourseDto.SyncStatusResponse status = new CourseDto.SyncStatusResponse();
        status.setYoutubeConfigured(true);   // no API key needed — scraping is always available
        status.setLastSyncAt(
                courseSyncScheduler.getLastSyncAt() != null
                        ? courseSyncScheduler.getLastSyncAt().format(FMT)
                        : "Never"
        );
        status.setLastSyncAdded(courseSyncScheduler.getLastSyncAdded());
        status.setLastSyncStatus(courseSyncScheduler.getLastSyncStatus());
        status.setTotalCourses(courseRepository.count());
        status.setAutoFetchedCourses(courseRepository.countByIsAutoFetchedTrue());
        status.setNextScheduledSync("Daily at 2:00 AM");
        status.setMessage(
                "Live scraping active — YouTube, Udemy & GitHub scanned daily. No API key required."
        );
        return status;
    }
}
