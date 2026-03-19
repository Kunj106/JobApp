package com.kunj.JobApp.dto;

import com.kunj.JobApp.entity.CompanyType;
import com.kunj.JobApp.entity.Job;
import com.kunj.JobApp.entity.JobType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class JobDto
{
    public static class JobRequest {
        @NotBlank private String title;
        @NotBlank private String company;
        @NotBlank private String location;
        @NotNull  private JobType jobType;
        @NotNull  private CompanyType companyType;
        private String description;
        private String requirements;
        private String salary;
        @NotBlank private String applyLink;
        private String experience;
        private String skills;

        // Getter & Setter

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getCompany() {
            return company;
        }

        public void setCompany(String company) {
            this.company = company;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        public JobType getJobType() {
            return jobType;
        }

        public void setJobType(JobType jobType) {
            this.jobType = jobType;
        }

        public CompanyType getCompanyType() {
            return companyType;
        }

        public void setCompanyType(CompanyType companyType) {
            this.companyType = companyType;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getRequirements() {
            return requirements;
        }

        public void setRequirements(String requirements) {
            this.requirements = requirements;
        }

        public String getSalary() {
            return salary;
        }

        public void setSalary(String salary) {
            this.salary = salary;
        }

        public String getApplyLink() {
            return applyLink;
        }

        public void setApplyLink(String applyLink) {
            this.applyLink = applyLink;
        }

        public String getExperience() {
            return experience;
        }

        public void setExperience(String experience) {
            this.experience = experience;
        }

        public String getSkills() {
            return skills;
        }

        public void setSkills(String skills) {
            this.skills = skills;
        }
    }

    public static class JobResponse {
        private Long id;
        private String title;
        private String company;
        private String location;
        private String jobType;
        private String companyType;
        private String description;
        private String requirements;
        private String salary;
        private String applyLink;
        private String experience;
        private String skills;
        private boolean isActive;
        private LocalDateTime postedAt;
        private LocalDateTime lastRefreshedAt;
        private String postedTimeAgo;   // human-readable "2 hours ago"
        private boolean isSaved;        // for logged-in users

        // Live-fetch extras
        private boolean isLive;           // fetched from external platform
        private boolean isRemote;
        private boolean isFresher;
        private boolean isNew;            // posted within last 24h
        private String  sourcePlatform;   // REMOTEOK, YC_JOBS, ADZUNA, UNSTOP…
        private String  addedTimeAgo;     // "3 hours ago"

        // Getter & Setter
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getCompany() {
            return company;
        }

        public void setCompany(String company) {
            this.company = company;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        public String getJobType() {
            return jobType;
        }

        public void setJobType(String jobType) {
            this.jobType = jobType;
        }

        public String getCompanyType() {
            return companyType;
        }

        public void setCompanyType(String companyType) {
            this.companyType = companyType;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getRequirements() {
            return requirements;
        }

        public void setRequirements(String requirements) {
            this.requirements = requirements;
        }

        public String getSalary() {
            return salary;
        }

        public void setSalary(String salary) {
            this.salary = salary;
        }

        public String getApplyLink() {
            return applyLink;
        }

        public void setApplyLink(String applyLink) {
            this.applyLink = applyLink;
        }

        public String getExperience() {
            return experience;
        }

        public void setExperience(String experience) {
            this.experience = experience;
        }

        public String getSkills() {
            return skills;
        }

        public void setSkills(String skills) {
            this.skills = skills;
        }

        public boolean isActive() {
            return isActive;
        }

        public void setActive(boolean active) {
            isActive = active;
        }

        public LocalDateTime getPostedAt() {
            return postedAt;
        }

        public void setPostedAt(LocalDateTime postedAt) {
            this.postedAt = postedAt;
        }

        public LocalDateTime getLastRefreshedAt() {
            return lastRefreshedAt;
        }

        public void setLastRefreshedAt(LocalDateTime lastRefreshedAt) {
            this.lastRefreshedAt = lastRefreshedAt;
        }

        public String getPostedTimeAgo() {
            return postedTimeAgo;
        }

        public void setPostedTimeAgo(String postedTimeAgo) {
            this.postedTimeAgo = postedTimeAgo;
        }

        public boolean isSaved() {
            return isSaved;
        }

        public void setSaved(boolean saved) {
            isSaved = saved;
        }

        public boolean isLive() {
            return isLive;
        }

        public void setLive(boolean live) {
            isLive = live;
        }

        public boolean isRemote() {
            return isRemote;
        }

        public void setRemote(boolean remote) {
            isRemote = remote;
        }

        public boolean isFresher() {
            return isFresher;
        }

        public void setFresher(boolean fresher) {
            isFresher = fresher;
        }

        public boolean isNew() {
            return isNew;
        }

        public void setNew(boolean aNew) {
            isNew = aNew;
        }

        public String getSourcePlatform() {
            return sourcePlatform;
        }

        public void setSourcePlatform(String sourcePlatform) {
            this.sourcePlatform = sourcePlatform;
        }

        public String getAddedTimeAgo() {
            return addedTimeAgo;
        }

        public void setAddedTimeAgo(String addedTimeAgo) {
            this.addedTimeAgo = addedTimeAgo;
        }

        public static JobResponse from(Job job) {
            JobResponse r = new JobResponse();
            r.setId(job.getId());
            r.setTitle(job.getTitle());
            r.setCompany(job.getCompany());
            r.setLocation(job.getLocation());
            r.setJobType(job.getJobType().name());
            r.setCompanyType(job.getCompanyType().name());
            r.setDescription(job.getDescription());
            r.setRequirements(job.getRequirements());
            r.setSalary(job.getSalary());
            r.setApplyLink(job.getApplyLink());
            r.setExperience(job.getExperience());
            r.setSkills(job.getSkills());
            r.setActive(job.isActive());
            r.setPostedAt(job.getPostedAt());
            r.setLastRefreshedAt(job.getLastRefreshedAt());
            r.setPostedTimeAgo(timeAgo(job.getLastRefreshedAt() != null
                    ? job.getLastRefreshedAt() : job.getPostedAt()));
            // Live-fetch fields
            r.setLive(job.isLive());
            r.setRemote(job.isRemote());
            r.setFresher(job.isFresher());
            r.setSourcePlatform(job.getSourcePlatform() != null
                    ? job.getSourcePlatform() : "MANUAL");
            r.setAddedTimeAgo(job.getPostedAt() != null ? timeAgo(job.getPostedAt()) : null);
            r.setNew(job.getPostedAt() != null &&
                    ChronoUnit.HOURS.between(job.getPostedAt(), LocalDateTime.now()) <= 24);
            return r;
        }

        private static String timeAgo(LocalDateTime dt) {
            if (dt == null) return "Just now";
            long minutes = ChronoUnit.MINUTES.between(dt, LocalDateTime.now());
            if (minutes < 1)   return "Just now";
            if (minutes < 60)  return minutes + " min ago";
            long hours = ChronoUnit.HOURS.between(dt, LocalDateTime.now());
            if (hours < 24)    return hours + (hours == 1 ? " hour ago" : " hours ago");
            long days = ChronoUnit.DAYS.between(dt, LocalDateTime.now());
            if (days < 7)      return days + (days == 1 ? " day ago" : " days ago");
            long weeks = days / 7;
            return weeks + (weeks == 1 ? " week ago" : " weeks ago");
        }
    }

    public static class JobFilterRequest {
        private String keyword;        // default: "java"
        private String location;       // default: "mumbai"
        private String companyType;    // STARTUP, MNC, MIDSIZE, FINTECH, BANKING
        private String jobType;        // JOB, INTERNSHIP
        private String timeFilter;     // "24H" = last 24 hours | "7W" = last 7 weeks | null = all
        private String sourceFilter;  // LIVE | MANUAL | REMOTE | FRESHER | null = all

        // Getter & Setter

        public String getKeyword() {
            return keyword;
        }

        public void setKeyword(String keyword) {
            this.keyword = keyword;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        public String getCompanyType() {
            return companyType;
        }

        public void setCompanyType(String companyType) {
            this.companyType = companyType;
        }

        public String getJobType() {
            return jobType;
        }

        public void setJobType(String jobType) {
            this.jobType = jobType;
        }

        public String getTimeFilter() {
            return timeFilter;
        }

        public void setTimeFilter(String timeFilter) {
            this.timeFilter = timeFilter;
        }

        public String getSourceFilter() {
            return sourceFilter;
        }

        public void setSourceFilter(String sourceFilter) {
            this.sourceFilter = sourceFilter;
        }
    }

    // Wrapper that includes metadata about the search refresh
    public static class JobSearchResult {
        private List<JobResponse> jobs;
        private int total;
        private LocalDateTime fetchedAt;
        private String timeFilter;
        private int last24hCount;
        private int last7wCount;
        private int liveCount;      // jobs from external platforms
        private int remoteCount;
        private int fresherCount;
        private int newCount;       // posted in last 24h

        // Getter & Setter
        public List<JobResponse> getJobs() {
            return jobs;
        }

        public void setJobs(List<JobResponse> jobs) {
            this.jobs = jobs;
        }

        public int getTotal() {
            return total;
        }

        public void setTotal(int total) {
            this.total = total;
        }

        public LocalDateTime getFetchedAt() {
            return fetchedAt;
        }

        public void setFetchedAt(LocalDateTime fetchedAt) {
            this.fetchedAt = fetchedAt;
        }

        public String getTimeFilter() {
            return timeFilter;
        }

        public void setTimeFilter(String timeFilter) {
            this.timeFilter = timeFilter;
        }

        public int getLast24hCount() {
            return last24hCount;
        }

        public void setLast24hCount(int last24hCount) {
            this.last24hCount = last24hCount;
        }

        public int getLast7wCount() {
            return last7wCount;
        }

        public void setLast7wCount(int last7wCount) {
            this.last7wCount = last7wCount;
        }

        public int getLiveCount() {
            return liveCount;
        }

        public void setLiveCount(int liveCount) {
            this.liveCount = liveCount;
        }

        public int getRemoteCount() {
            return remoteCount;
        }

        public void setRemoteCount(int remoteCount) {
            this.remoteCount = remoteCount;
        }

        public int getFresherCount() {
            return fresherCount;
        }

        public void setFresherCount(int fresherCount) {
            this.fresherCount = fresherCount;
        }

        public int getNewCount() {
            return newCount;
        }

        public void setNewCount(int newCount) {
            this.newCount = newCount;
        }

        public JobSearchResult(List<JobResponse> jobs, String timeFilter) {
            this.jobs = jobs;
            this.total = jobs.size();
            this.fetchedAt = LocalDateTime.now();
            this.timeFilter = timeFilter != null ? timeFilter : "ALL";
            this.last24hCount = (int) jobs.stream()
                    .filter(j -> j.getLastRefreshedAt() != null &&
                            ChronoUnit.HOURS.between(j.getLastRefreshedAt(), LocalDateTime.now()) <= 24)
                    .count();
            this.last7wCount = (int) jobs.stream()
                    .filter(j -> j.getLastRefreshedAt() != null &&
                            ChronoUnit.DAYS.between(j.getLastRefreshedAt(), LocalDateTime.now()) <= 49)
                    .count();
            this.liveCount    = (int) jobs.stream().filter(JobResponse::isLive).count();
            this.remoteCount  = (int) jobs.stream().filter(JobResponse::isRemote).count();
            this.fresherCount = (int) jobs.stream().filter(JobResponse::isFresher).count();
            this.newCount     = (int) jobs.stream().filter(JobResponse::isNew).count();
        }
    }
    //  Sync status DTO (for /api/jobs/sync/status)
    public static class JobSyncStatusResponse {
        private boolean syncEnabled;
        private String  lastSyncAt;
        private int     lastSyncAdded;
        private String  lastSyncStatus;
        private long    totalJobs;
        private long    liveJobs;
        private long    newJobsToday;
        private String  nextScheduledSync;
        private String  message;
        private PlatformBreakdown platforms;

        // Getter & Setter
        public boolean isSyncEnabled() {
            return syncEnabled;
        }

        public void setSyncEnabled(boolean syncEnabled) {
            this.syncEnabled = syncEnabled;
        }

        public String getLastSyncAt() {
            return lastSyncAt;
        }

        public void setLastSyncAt(String lastSyncAt) {
            this.lastSyncAt = lastSyncAt;
        }

        public int getLastSyncAdded() {
            return lastSyncAdded;
        }

        public void setLastSyncAdded(int lastSyncAdded) {
            this.lastSyncAdded = lastSyncAdded;
        }

        public String getLastSyncStatus() {
            return lastSyncStatus;
        }

        public void setLastSyncStatus(String lastSyncStatus) {
            this.lastSyncStatus = lastSyncStatus;
        }

        public long getTotalJobs() {
            return totalJobs;
        }

        public void setTotalJobs(long totalJobs) {
            this.totalJobs = totalJobs;
        }

        public long getLiveJobs() {
            return liveJobs;
        }

        public void setLiveJobs(long liveJobs) {
            this.liveJobs = liveJobs;
        }

        public long getNewJobsToday() {
            return newJobsToday;
        }

        public void setNewJobsToday(long newJobsToday) {
            this.newJobsToday = newJobsToday;
        }

        public String getNextScheduledSync() {
            return nextScheduledSync;
        }

        public void setNextScheduledSync(String nextScheduledSync) {
            this.nextScheduledSync = nextScheduledSync;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public PlatformBreakdown getPlatforms() {
            return platforms;
        }

        public void setPlatforms(PlatformBreakdown platforms) {
            this.platforms = platforms;
        }
    }

    public static class PlatformBreakdown {
        private long remoteOk;
        private long ycJobs;
        private long adzuna;
        private long unstop;
        private long internshala;
        private long wellfound;
        private long manual;

        // Getter & Setter
        public long getRemoteOk() {
            return remoteOk;
        }

        public void setRemoteOk(long remoteOk) {
            this.remoteOk = remoteOk;
        }

        public long getYcJobs() {
            return ycJobs;
        }

        public void setYcJobs(long ycJobs) {
            this.ycJobs = ycJobs;
        }

        public long getAdzuna() {
            return adzuna;
        }

        public void setAdzuna(long adzuna) {
            this.adzuna = adzuna;
        }

        public long getUnstop() {
            return unstop;
        }

        public void setUnstop(long unstop) {
            this.unstop = unstop;
        }

        public long getInternshala() {
            return internshala;
        }

        public void setInternshala(long internshala) {
            this.internshala = internshala;
        }

        public long getWellfound() {
            return wellfound;
        }

        public void setWellfound(long wellfound) {
            this.wellfound = wellfound;
        }

        public long getManual() {
            return manual;
        }

        public void setManual(long manual) {
            this.manual = manual;
        }
    }
}
