package com.kunj.JobApp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "free_courses")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FreeCourse
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String platform;      // YouTube, Udemy, Coursera, etc.

    @Column(nullable = false)
    private String technology;    // Java, Spring Boot, AWS, etc.

    @Column(columnDefinition = "TEXT")
    private String description;

    private String instructor;

    private String duration;      // e.g. "12 hours", "30 hours"

    private String level;         // Beginner, Intermediate, Advanced

    @Column(nullable = false)
    private String courseLink;

    private String thumbnailUrl;

    @Enumerated(EnumType.STRING)
    private CourseCategory category;

    // ── Auto-fetch metadata ──────────────────────────────────────────────────
    @Column(unique = true)
    private String sourceId;          // YouTube video/playlist ID — prevents duplicates

    private String sourceType;        // "YOUTUBE", "MANUAL"

    private java.time.LocalDateTime fetchedAt;  // when this course was auto-discovered

    private boolean isAutoFetched;    // true = pulled from YouTube, false = manually seeded

    @PrePersist
    public void prePersist() {
        if (this.fetchedAt == null) this.fetchedAt = java.time.LocalDateTime.now();
        if (this.sourceType == null) this.sourceType = "MANUAL";
    }

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

    public CourseCategory getCategory() {
        return category;
    }

    public void setCategory(CourseCategory category) {
        this.category = category;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
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

    public boolean isAutoFetched() {
        return isAutoFetched;
    }

    public void setAutoFetched(boolean autoFetched) {
        isAutoFetched = autoFetched;
    }
}
