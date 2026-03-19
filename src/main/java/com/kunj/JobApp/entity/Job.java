package com.kunj.JobApp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "jobs")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Job
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String company;

    @Column(nullable = false)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobType jobType;         // JOB or INTERNSHIP

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CompanyType companyType; // STARTUP, MNC, MIDSIZE, FINTECH, BANKING

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String requirements;

    private String salary;

    @Column(nullable = false)
    private String applyLink;

    private String experience;       // e.g. "0-1 years", "Fresher"

    @Column(columnDefinition = "TEXT")
    private String skills;           // comma-separated: "Java, Spring Boot, MySQL"

    private boolean isActive;

    @Column(updatable = false)
    private LocalDateTime postedAt;

    // Updated every time the job is fetched/refreshed via a live search
    private LocalDateTime lastRefreshedAt;

    @Column(unique = true)
    private String sourceId;          // unique ID to prevent duplicates (platform_jobid)

    private String sourcePlatform;    // "REMOTEOK", "YC_JOBS", "ADZUNA", "UNSTOP",
    // "INTERNSHALA", "WELLFOUND", "MANUAL"

    private boolean isLive;           // true = fetched from external platform
    private boolean isRemote;         // true = remote/WFH job
    private boolean isFresher;

    @PrePersist
    public void prePersist() {
        this.postedAt = LocalDateTime.now();
        this.lastRefreshedAt = LocalDateTime.now();
        this.isActive = true;
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

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public String getSourcePlatform() {
        return sourcePlatform;
    }

    public void setSourcePlatform(String sourcePlatform) {
        this.sourcePlatform = sourcePlatform;
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
}
