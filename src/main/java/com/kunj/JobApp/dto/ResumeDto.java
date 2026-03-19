package com.kunj.JobApp.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

public class ResumeDto
{
    public static class TailorRequest {
        @NotBlank(message = "Resume text is required")
        private String resumeText;

        @NotBlank(message = "Job description is required")
        private String jobDescription;

        @NotBlank(message = "Job title is required")
        private String jobTitle;

        @NotBlank(message = "Company name is required")
        private String companyName;

        // Getter & Setter

        public String getResumeText() {
            return resumeText;
        }

        public void setResumeText(String resumeText) {
            this.resumeText = resumeText;
        }

        public String getJobDescription() {
            return jobDescription;
        }

        public void setJobDescription(String jobDescription) {
            this.jobDescription = jobDescription;
        }

        public String getJobTitle() {
            return jobTitle;
        }

        public void setJobTitle(String jobTitle) {
            this.jobTitle = jobTitle;
        }

        public String getCompanyName() {
            return companyName;
        }

        public void setCompanyName(String companyName) {
            this.companyName = companyName;
        }
    }

    public static class TailorResponse {
        private Long historyId;
        private String tailoredResume;
        private String jobTitle;
        private String companyName;
        private String tips;
        private LocalDateTime createdAt;

        // Getter & Setter

        public Long getHistoryId() {
            return historyId;
        }

        public void setHistoryId(Long historyId) {
            this.historyId = historyId;
        }

        public String getTailoredResume() {
            return tailoredResume;
        }

        public void setTailoredResume(String tailoredResume) {
            this.tailoredResume = tailoredResume;
        }

        public String getJobTitle() {
            return jobTitle;
        }

        public void setJobTitle(String jobTitle) {
            this.jobTitle = jobTitle;
        }

        public String getCompanyName() {
            return companyName;
        }

        public void setCompanyName(String companyName) {
            this.companyName = companyName;
        }

        public String getTips() {
            return tips;
        }

        public void setTips(String tips) {
            this.tips = tips;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
        }
    }

    public static class ResumeHistoryResponse {
        private Long id;
        private String jobTitle;
        private String companyName;
        private String tailoredResume;
        private LocalDateTime createdAt;

        // Getter & Setter

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getJobTitle() {
            return jobTitle;
        }

        public void setJobTitle(String jobTitle) {
            this.jobTitle = jobTitle;
        }

        public String getCompanyName() {
            return companyName;
        }

        public void setCompanyName(String companyName) {
            this.companyName = companyName;
        }

        public String getTailoredResume() {
            return tailoredResume;
        }

        public void setTailoredResume(String tailoredResume) {
            this.tailoredResume = tailoredResume;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
        }
    }

    public static class UpdateResumeRequest {
        @NotBlank(message = "Resume text is required")
        private String resumeText;

        // Getter & Setter

        public String getResumeText() {
            return resumeText;
        }

        public void setResumeText(String resumeText) {
            this.resumeText = resumeText;
        }
    }
}
