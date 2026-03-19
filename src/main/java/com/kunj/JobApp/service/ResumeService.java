package com.kunj.JobApp.service;

import com.kunj.JobApp.dto.ResumeDto;
import com.kunj.JobApp.entity.ResumeHistory;
import com.kunj.JobApp.entity.User;
import com.kunj.JobApp.exception.ApiException;
import com.kunj.JobApp.repository.ResumeHistoryRepository;
import com.kunj.JobApp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResumeService {
    private final UserRepository userRepository;
    private final ResumeHistoryRepository resumeHistoryRepository;
    private final ObjectMapper objectMapper;

    @Value("${openrouter.api.key}")
    private String openRouterApiKey;

    @Value("${openrouter.api.url}")
    private String openRouterUrl;

    @Value("${openrouter.model}")
    private String model;

    // ── Tailor Resume ─────────────────────────────────────────────────────────

    public ResumeDto.TailorResponse tailorResume(ResumeDto.TailorRequest request, String userEmail) {
        String tailoredContent = callOpenRouter(request.getResumeText(), request.getJobDescription(),
                request.getJobTitle(), request.getCompanyName());

        // Parse the response — model returns JSON with tailoredResume + tips
        String tailoredResume;
        String tips;
        try {
            JsonNode node = objectMapper.readTree(tailoredContent);
            tailoredResume = node.has("tailoredResume") ? node.get("tailoredResume").asText() : tailoredContent;
            tips = node.has("tips") ? node.get("tips").asText() : "";
        } catch (Exception e) {
            // If model didn't return JSON, use raw text
            tailoredResume = tailoredContent;
            tips = "";
        }

        // Save history if user is logged in
        Long historyId = null;
        if (userEmail != null) {
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));
            ResumeHistory history = ResumeHistory.builder()
                    .user(user)
                    .jobDescription(request.getJobDescription())
                    .jobTitle(request.getJobTitle())
                    .companyName(request.getCompanyName())
                    .originalResume(request.getResumeText())
                    .tailoredResume(tailoredResume)
                    .build();
            historyId = resumeHistoryRepository.save(history).getId();
        }

        ResumeDto.TailorResponse response = new ResumeDto.TailorResponse();
        response.setHistoryId(historyId);
        response.setTailoredResume(tailoredResume);
        response.setJobTitle(request.getJobTitle());
        response.setCompanyName(request.getCompanyName());
        response.setTips(tips);
        return response;
    }

    // ── History ───────────────────────────────────────────────────────────────

    public List<ResumeDto.ResumeHistoryResponse> getHistory(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));
        return resumeHistoryRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream().map(h -> {
                    ResumeDto.ResumeHistoryResponse r = new ResumeDto.ResumeHistoryResponse();
                    r.setId(h.getId());
                    r.setJobTitle(h.getJobTitle());
                    r.setCompanyName(h.getCompanyName());
                    r.setTailoredResume(h.getTailoredResume());
                    r.setCreatedAt(h.getCreatedAt());
                    return r;
                }).collect(Collectors.toList());
    }

    // ── Save Base Resume ──────────────────────────────────────────────────────

    public void saveBaseResume(String resumeText, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));
        user.setResumeText(resumeText);
        userRepository.save(user);
    }

    public String getBaseResume(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));
        return user.getResumeText() != null ? user.getResumeText() : "";
    }

    // ── OpenRouter API Call ───────────────────────────────────────────────────

    private String callOpenRouter(String resume, String jobDesc, String jobTitle, String company) {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();

        String prompt = buildPrompt(resume, jobDesc, jobTitle, company);

        String requestBody;
        try {
            requestBody = objectMapper.writeValueAsString(new java.util.LinkedHashMap<>() {{
                put("model", model);
                put("max_tokens", 2000);
                put("messages", List.of(
                        new java.util.LinkedHashMap<>() {{
                            put("role", "system");
                            put("content", "You are an expert resume writer and career coach specializing in Java development roles. You tailor resumes to match job descriptions perfectly while keeping all information truthful.");
                        }},
                        new java.util.LinkedHashMap<>() {{
                            put("role", "user");
                            put("content", prompt);
                        }}
                ));
            }});
        } catch (Exception e) {
            throw new ApiException("Failed to build AI request", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        Request request = new Request.Builder()
                .url(openRouterUrl)
                .post(RequestBody.create(requestBody, MediaType.parse("application/json")))
                .addHeader("Authorization", "Bearer " + openRouterApiKey)
                .addHeader("Content-Type", "application/json")
                .addHeader("HTTP-Referer", "http://localhost:8080")
                .addHeader("X-Title", "Java Career Dashboard")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errBody = response.body() != null ? response.body().string() : "No error body";
                log.error("OpenRouter error {}: {}", response.code(), errBody);
                throw new ApiException("AI service error: " + response.code(), HttpStatus.BAD_GATEWAY);
            }
            String body = response.body().string();
            JsonNode json = objectMapper.readTree(body);
            return json.at("/choices/0/message/content").asText();
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("OpenRouter call failed", e);
            throw new ApiException("Failed to connect to AI service: " + e.getMessage(),
                    HttpStatus.SERVICE_UNAVAILABLE);
        }
    }
    private String buildPrompt(String resume, String jobDesc, String jobTitle, String company) {
        return String.format("""
            I need you to tailor my resume for a specific job. 
            
            JOB TITLE: %s
            COMPANY: %s
            
            JOB DESCRIPTION:
            %s
            
            MY CURRENT RESUME:
            %s
            
            Please rewrite my resume to:
            1. Highlight skills and experience that match the job description
            2. Use keywords from the job description naturally
            3. Reorder and emphasize relevant bullet points
            4. Keep all facts truthful - don't fabricate experience
            5. Make the objective/summary match this specific role
            6. Ensure ATS (Applicant Tracking System) compatibility
            
            Respond ONLY with a valid JSON object in this exact format:
            {
              "tailoredResume": "the full tailored resume text here",
              "tips": "3-5 bullet point tips on why this resume now matches the role better"
            }
            """,
                jobTitle, company, jobDesc, resume);
    }
}
