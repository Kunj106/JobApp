package com.kunj.JobApp.controller;

import com.kunj.JobApp.config.CourseSyncScheduler;
import com.kunj.JobApp.dto.CourseDto;
import com.kunj.JobApp.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController
{
    private final CourseService courseService;

    /** GET /api/courses */
    @GetMapping
    public ResponseEntity<List<CourseDto.CourseResponse>> getAllCourses() {
        return ResponseEntity.ok(courseService.getAllCourses());
    }

    /** GET /api/courses/grouped */
    @GetMapping("/grouped")
    public ResponseEntity<Map<String, List<CourseDto.CourseResponse>>> getGrouped() {
        return ResponseEntity.ok(courseService.getCourseGroupedByTechnology());
    }

    /** GET /api/courses/category/{category} */
    @GetMapping("/category/{category}")
    public ResponseEntity<List<CourseDto.CourseResponse>> getByCategory(
            @PathVariable String category) {
        return ResponseEntity.ok(courseService.getCoursesByCategory(category));
    }

    /** GET /api/courses/search?query=spring */
    @GetMapping("/search")
    public ResponseEntity<List<CourseDto.CourseResponse>> searchCourses(
            @RequestParam String query) {
        return ResponseEntity.ok(courseService.searchCourses(query));
    }

    /** GET /api/courses/{id} */
    @GetMapping("/{id}")
    public ResponseEntity<CourseDto.CourseResponse> getCourseById(@PathVariable Long id) {
        return ResponseEntity.ok(courseService.getCourseById(id));
    }

    /**
     * GET /api/courses/sync/status
     * Returns sync metadata: last sync time, courses added, YouTube configured.
     */
    @GetMapping("/sync/status")
    public ResponseEntity<CourseDto.SyncStatusResponse> getSyncStatus() {
        return ResponseEntity.ok(courseService.getSyncStatus());
    }

    /**
     * POST /api/courses/sync
     * Manually triggers a YouTube course sync.
     * Called from the frontend "🔄 Sync New Courses" button.
     */
    @PostMapping("/sync")
    public ResponseEntity<CourseSyncScheduler.SyncResult> triggerSync() {
        return ResponseEntity.ok(courseService.triggerSync());
    }
}
