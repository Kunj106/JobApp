package com.kunj.JobApp.service;

import com.kunj.JobApp.config.JobSyncScheduler;
import com.kunj.JobApp.dto.JobDto;
import com.kunj.JobApp.entity.*;
import com.kunj.JobApp.exception.ApiException;
import com.kunj.JobApp.repository.JobRepository;
import com.kunj.JobApp.repository.SavedJobRepository;
import com.kunj.JobApp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobService
{
    private final JobRepository     jobRepository;
    private final SavedJobRepository savedJobRepository;
    private final UserRepository    userRepository;
    private final JobSyncScheduler jobSyncScheduler;

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    //Search + Live Refresh
    @Transactional
    public JobDto.JobSearchResult searchJobs(JobDto.JobFilterRequest filter, String userEmail) {
        String keyword  = blank(filter.getKeyword(),  "java").toLowerCase();
        String location = blank(filter.getLocation(), "").toLowerCase();

        CompanyType companyType = parseEnum(CompanyType.class, filter.getCompanyType());
        JobType     jobType     = parseEnum(JobType.class,     filter.getJobType());
        String          timeFilter  = filter.getTimeFilter();
        String          srcFilter   = filter.getSourceFilter();

        // Bump lastRefreshedAt on all matching jobs = "live refresh" indicator
        List<Long> ids = jobRepository.findIdsByKeywordAndLocation(keyword, location);
        if (!ids.isEmpty()) jobRepository.refreshByIds(ids, LocalDateTime.now());

        // Determine time window
        LocalDateTime since = null;
        if      ("24H".equalsIgnoreCase(timeFilter)) since = LocalDateTime.now().minusHours(24);
        else if ("7W".equalsIgnoreCase(timeFilter))  since = LocalDateTime.now().minusWeeks(7);

        List<Job> jobs = since != null
                ? jobRepository.findWithFiltersAndSince(companyType, jobType, keyword, location, since)
                : jobRepository.findWithFilters(companyType, jobType, keyword, location);

        // Apply source filter (LIVE | MANUAL | REMOTE | FRESHER | NEW)
        if (srcFilter != null && !srcFilter.isBlank()) {
            jobs = switch (srcFilter.toUpperCase()) {
                case "LIVE"       -> jobs.stream().filter(Job::isLive).toList();
                case "MANUAL"     -> jobs.stream().filter(j -> !j.isLive()).toList();
                case "REMOTE" -> jobs.stream()
                        .filter(Job::isRemote)
                        .filter(j -> {
                            String t = ((j.getTitle()  != null ? j.getTitle()  : "") + " " +
                                    (j.getSkills() != null ? j.getSkills() : "")).toLowerCase();
                            // Must have Java keyword AND must not be a frontend role
                            boolean hasJava = t.contains("java") || t.contains("spring")
                                    || t.contains("backend") || t.contains("microservice");
                            boolean isFrontend = t.contains("frontend") || t.contains("react")
                                    || t.contains("angular") || t.contains("vue") || t.contains("css");
                            return hasJava && !isFrontend;
                        }).toList();
                case "FRESHER"    -> jobs.stream().filter(Job::isFresher).toList();
                case "NEW"        -> jobs.stream().filter(j -> j.getPostedAt() != null &&
                        java.time.temporal.ChronoUnit.HOURS.between(
                                j.getPostedAt(), LocalDateTime.now()) <= 24).toList();
                default           -> jobs;
            };
        }

        Long userId = getUserId(userEmail);
        List<JobDto.JobResponse> responses = jobs.stream().map(job -> {
            JobDto.JobResponse r = JobDto.JobResponse.from(job);
            if (userId != null)
                r.setSaved(savedJobRepository.existsByUserIdAndJobId(userId, job.getId()));
            return r;
        }).collect(Collectors.toList());

        return new JobDto.JobSearchResult(responses, timeFilter);
    }

    public JobDto.JobResponse getJobById(Long id, String userEmail) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new ApiException("Job not found", HttpStatus.NOT_FOUND));
        JobDto.JobResponse r = JobDto.JobResponse.from(job);
        Long userId = getUserId(userEmail);
        if (userId != null)
            r.setSaved(savedJobRepository.existsByUserIdAndJobId(userId, job.getId()));
        return r;
    }

    // Sync Status
    public JobDto.JobSyncStatusResponse getSyncStatus() {
        JobDto.JobSyncStatusResponse s = new JobDto.JobSyncStatusResponse();
        s.setSyncEnabled(true);
        s.setLastSyncAt(jobSyncScheduler.getLastSyncAt() != null
                ? jobSyncScheduler.getLastSyncAt().format(FMT) : "Never");
        s.setLastSyncAdded(jobSyncScheduler.getLastSyncAdded());
        s.setLastSyncStatus(jobSyncScheduler.getLastSyncStatus());
        s.setTotalJobs(jobRepository.count());
        s.setLiveJobs(jobRepository.countByIsLiveTrue());
        s.setNewJobsToday(jobRepository.countByIsLiveTrueAndPostedAtAfter(
                LocalDateTime.now().minusHours(24)));
        s.setNextScheduledSync("Daily at 1:00 AM");
        s.setMessage("Live jobs fetched from RemoteOK · YC Jobs · Adzuna · Unstop · Internshala · Wellfound");

        JobDto.PlatformBreakdown pb = new JobDto.PlatformBreakdown();
        pb.setRemoteOk(jobRepository.countBySourcePlatform("REMOTEOK"));
        pb.setYcJobs(jobRepository.countBySourcePlatform("YC_JOBS"));
        pb.setAdzuna(jobRepository.countBySourcePlatform("ADZUNA"));
        pb.setUnstop(jobRepository.countBySourcePlatform("UNSTOP"));
        pb.setInternshala(jobRepository.countBySourcePlatform("INTERNSHALA"));
        pb.setWellfound(jobRepository.countBySourcePlatform("WELLFOUND"));
        pb.setManual(jobRepository.countBySourcePlatform("MANUAL"));
        s.setPlatforms(pb);
        return s;
    }

    public JobSyncScheduler.SyncResult triggerSync() {
        return jobSyncScheduler.manualSync();
    }

    // CRUD
    public JobDto.JobResponse createJob(JobDto.JobRequest req) {
        Job job = Job.builder()
                .title(req.getTitle()).company(req.getCompany())
                .location(req.getLocation()).jobType(req.getJobType())
                .companyType(req.getCompanyType()).description(req.getDescription())
                .requirements(req.getRequirements()).salary(req.getSalary())
                .applyLink(req.getApplyLink()).experience(req.getExperience())
                .skills(req.getSkills()).sourcePlatform("MANUAL")
                .build();
        return JobDto.JobResponse.from(jobRepository.save(job));
    }

    public JobDto.JobResponse updateJob(Long id, JobDto.JobRequest req) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new ApiException("Job not found", HttpStatus.NOT_FOUND));
        job.setTitle(req.getTitle());         job.setCompany(req.getCompany());
        job.setLocation(req.getLocation());   job.setJobType(req.getJobType());
        job.setCompanyType(req.getCompanyType()); job.setDescription(req.getDescription());
        job.setRequirements(req.getRequirements()); job.setSalary(req.getSalary());
        job.setApplyLink(req.getApplyLink()); job.setExperience(req.getExperience());
        job.setSkills(req.getSkills());
        return JobDto.JobResponse.from(jobRepository.save(job));
    }

    public void deleteJob(Long id) { jobRepository.deleteById(id); }

    //  Save / Unsave
    @Transactional
    public String toggleSaveJob(Long jobId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ApiException("Job not found", HttpStatus.NOT_FOUND));
        if (savedJobRepository.existsByUserIdAndJobId(user.getId(), jobId)) {
            savedJobRepository.deleteByUserIdAndJobId(user.getId(), jobId);
            return "Job removed from saved list";
        }
        savedJobRepository.save(SavedJob.builder().user(user).job(job).build());
        return "Job saved successfully";
    }

    public List<JobDto.JobResponse> getSavedJobs(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));
        return savedJobRepository.findByUserId(user.getId()).stream()
                .map(s -> { JobDto.JobResponse r = JobDto.JobResponse.from(s.getJob());
                    r.setSaved(true); return r; })
                .collect(Collectors.toList());
    }

    //  Helpers
    private Long getUserId(String email) {
        if (email == null) return null;
        return userRepository.findByEmail(email).map(User::getId).orElse(null);
    }

    private String blank(String val, String def) {
        return (val == null || val.isBlank()) ? def : val;
    }

    @SuppressWarnings("unchecked")
    private <T extends Enum<T>> T parseEnum(Class<T> clazz, String value) {
        if (value == null || value.isBlank()) return null;
        try { return Enum.valueOf(clazz, value.toUpperCase()); }
        catch (IllegalArgumentException e) { return null; }
    }
}
