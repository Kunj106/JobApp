package com.kunj.JobApp.dto;

import com.kunj.JobApp.entity.FreeCourse;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class CourseDto
{
    public static class CourseResponse {
        private Long id;
        private String title;
        private String platform;
        private String technology;
        private String description;
        private String instructor;
        private String duration;
        private String level;
        private String courseLink;
        private String thumbnailUrl;
        private String category;
        private boolean isAutoFetched;      // true = from YouTube live fetch
        private String sourceType;          // "YOUTUBE" or "MANUAL"
        private LocalDateTime fetchedAt;
        private boolean isNew;              // added in last 48 hours
        private String addedTimeAgo;        // "2 hours ago", "3 days ago"

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

        public String getPlatform() {
            return platform;
        }

        public void setPlatform(String platform) {
            this.platform = platform;
        }

        public String getTechnology() {
            return technology;
        }

        public void setTechnology(String technology) {
            this.technology = technology;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getInstructor() {
            return instructor;
        }

        public void setInstructor(String instructor) {
            this.instructor = instructor;
        }

        public String getDuration() {
            return duration;
        }

        public void setDuration(String duration) {
            this.duration = duration;
        }

        public String getLevel() {
            return level;
        }

        public void setLevel(String level) {
            this.level = level;
        }

        public String getCourseLink() {
            return courseLink;
        }

        public void setCourseLink(String courseLink) {
            this.courseLink = courseLink;
        }

        public String getThumbnailUrl() {
            return thumbnailUrl;
        }

        public void setThumbnailUrl(String thumbnailUrl) {
            this.thumbnailUrl = thumbnailUrl;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public boolean isAutoFetched() {
            return isAutoFetched;
        }

        public void setAutoFetched(boolean autoFetched) {
            isAutoFetched = autoFetched;
        }

        public String getSourceType() {
            return sourceType;
        }

        public void setSourceType(String sourceType) {
            this.sourceType = sourceType;
        }

        public LocalDateTime getFetchedAt() {
            return fetchedAt;
        }

        public void setFetchedAt(LocalDateTime fetchedAt) {
            this.fetchedAt = fetchedAt;
        }

        public boolean isNew() {
            return isNew;
        }

        public void setNew(boolean aNew) {
            isNew = aNew;
        }

        public String getAddedTimeAgo() {
            return addedTimeAgo;
        }

        public void setAddedTimeAgo(String addedTimeAgo) {
            this.addedTimeAgo = addedTimeAgo;
        }

        public static CourseResponse from(FreeCourse c) {
            CourseResponse r = new CourseResponse();
            r.setId(c.getId());
            r.setTitle(c.getTitle());
            r.setPlatform(c.getPlatform());
            r.setTechnology(c.getTechnology());
            r.setDescription(c.getDescription());
            r.setInstructor(c.getInstructor());
            r.setDuration(c.getDuration());
            r.setLevel(c.getLevel());
            r.setCourseLink(c.getCourseLink());
            r.setThumbnailUrl(c.getThumbnailUrl());
            r.setCategory(c.getCategory() != null ? c.getCategory().name() : null);
            r.setAutoFetched(c.isAutoFetched());
            r.setSourceType(c.getSourceType());
            r.setFetchedAt(c.getFetchedAt());
            // "NEW" = added within last 48 hours
            if (c.getFetchedAt() != null) {
                long hours = ChronoUnit.HOURS.between(c.getFetchedAt(), LocalDateTime.now());
                r.setNew(hours <= 48);
                r.setAddedTimeAgo(timeAgo(c.getFetchedAt()));
            }
            return r;
        }

        private static String timeAgo(LocalDateTime dt) {
            long mins  = ChronoUnit.MINUTES.between(dt, LocalDateTime.now());
            if (mins < 1)   return "Just now";
            if (mins < 60)  return mins + " min ago";
            long hrs = ChronoUnit.HOURS.between(dt, LocalDateTime.now());
            if (hrs < 24)   return hrs + (hrs == 1 ? " hour ago" : " hours ago");
            long days = ChronoUnit.DAYS.between(dt, LocalDateTime.now());
            if (days < 7)   return days + (days == 1 ? " day ago" : " days ago");
            return (days / 7) + " week" + (days / 7 == 1 ? "" : "s") + " ago";
        }
    }

    /** Response for the sync status / manual trigger endpoint */
    public static class SyncStatusResponse {
        private boolean youtubeConfigured;
        private String lastSyncAt;
        private int lastSyncAdded;
        private String lastSyncStatus;
        private long totalCourses;
        private long autoFetchedCourses;
        private String nextScheduledSync;
        private String message;

        // Getter & Setter

        public boolean isYoutubeConfigured() {
            return youtubeConfigured;
        }

        public void setYoutubeConfigured(boolean youtubeConfigured) {
            this.youtubeConfigured = youtubeConfigured;
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

        public long getTotalCourses() {
            return totalCourses;
        }

        public void setTotalCourses(long totalCourses) {
            this.totalCourses = totalCourses;
        }

        public long getAutoFetchedCourses() {
            return autoFetchedCourses;
        }

        public void setAutoFetchedCourses(long autoFetchedCourses) {
            this.autoFetchedCourses = autoFetchedCourses;
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
    }
}
