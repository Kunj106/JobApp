package com.kunj.JobApp.controller;

import com.kunj.JobApp.config.JobSyncScheduler;
import com.kunj.JobApp.dto.JobDto;
import com.kunj.JobApp.service.JobEmailService;
import com.kunj.JobApp.service.JobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController
{
    private final JobService jobService;

    private final JobEmailService jobEmailService;

//      GET /api/jobs/search
//        ?keyword=java
//        &location=mumbai
//        &companyType=MNC          (optional)
//       &jobType=JOB              (optional)
//        &timeFilter=24H           (optional: 24H | 7W | omit for all)
//
//      On every call:
//       - Bumps lastRefreshedAt on all matching jobs (live refresh)
//       - Filters by time window if timeFilter is set
//      - Returns JobSearchResult with metadata (fetchedAt, counts)

    // GET /api/jobs/search
    @GetMapping("/search")
    public ResponseEntity<JobDto.JobSearchResult> searchJobs(
            @RequestParam(defaultValue = "java")   String keyword,
            @RequestParam(defaultValue = "")       String location,
            @RequestParam(required = false)        String companyType,
            @RequestParam(required = false)        String jobType,
            @RequestParam(required = false)        String timeFilter,
            @RequestParam(required = false)        String sourceFilter,
            Authentication auth) {

        JobDto.JobFilterRequest filter = new JobDto.JobFilterRequest();
        filter.setKeyword(keyword);
        filter.setLocation(location);
        filter.setCompanyType(companyType);
        filter.setJobType(jobType);
        filter.setTimeFilter(timeFilter);
        filter.setSourceFilter(sourceFilter);

        String email = auth != null ? auth.getName() : null;
        return ResponseEntity.ok(jobService.searchJobs(filter, email));
    }

    // GET /api/jobs/{id}
    @GetMapping("/{id}")
    public ResponseEntity<JobDto.JobResponse> getJobById(
            @PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(jobService.getJobById(id,
                auth != null ? auth.getName() : null));
    }

    //GET /api/jobs/sync/status
    @GetMapping("/sync/status")
    public ResponseEntity<JobDto.JobSyncStatusResponse> getSyncStatus() {
        return ResponseEntity.ok(jobService.getSyncStatus());
    }

    // POST /api/jobs/sync — trigger manual sync from UI
    @PostMapping("/sync")
    public ResponseEntity<Map<String, Object>> triggerSync() {
        JobSyncScheduler.SyncResult result = jobService.triggerSync();
        return ResponseEntity.ok(Map.of(
                "status",  result.status(),
                "added",   result.added(),
                "message", result.message()
        ));
    }

    // POST /api/jobs
    @PostMapping
    public ResponseEntity<JobDto.JobResponse> createJob(
            @Valid @RequestBody JobDto.JobRequest request) {
        return ResponseEntity.ok(jobService.createJob(request));
    }

    //PUT /api/jobs/{id}
    @PutMapping("/{id}")
    public ResponseEntity<JobDto.JobResponse> updateJob(
            @PathVariable Long id, @Valid @RequestBody JobDto.JobRequest request) {
        return ResponseEntity.ok(jobService.updateJob(id, request));
    }

    // DELETE /api/jobs/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteJob(@PathVariable Long id) {
        jobService.deleteJob(id);
        return ResponseEntity.noContent().build();
    }

    // POST /api/jobs/save/{jobId}
    @PostMapping("/save/{jobId}")
    public ResponseEntity<Map<String, String>> toggleSave(
            @PathVariable Long jobId, Authentication auth) {
        return ResponseEntity.ok(Map.of("message",
                jobService.toggleSaveJob(jobId, auth.getName())));
    }

    // GET /api/jobs/saved
    @GetMapping("/saved")
    public ResponseEntity<List<JobDto.JobResponse>> getSavedJobs(Authentication auth) {
        return ResponseEntity.ok(jobService.getSavedJobs(auth.getName()));
    }

    @PostMapping("/email/test")
    public ResponseEntity<Map<String, String>> testEmail() {
        try {
            jobEmailService.sendDailyDigest();
            return ResponseEntity.ok(Map.of(
                    "status",  "SUCCESS",
                    "message", "Daily digest email sent successfully — check your inbox!"
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "status",  "FAILED",
                    "message", e.getMessage()
            ));
        }
    }
}
