package com.kunj.JobApp.controller;

import com.kunj.JobApp.dto.ResumeDto;
import com.kunj.JobApp.service.ResumeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/resume")
@RequiredArgsConstructor
public class ResumeController
{
    private final ResumeService resumeService;

     // POST /api/resume/tailor
     // Tailor resume using AI (free Mistral via OpenRouter)
     // Body: { resumeText, jobDescription, jobTitle, companyName }
    @PostMapping("/tailor")
    public ResponseEntity<ResumeDto.TailorResponse> tailorResume(
            @Valid @RequestBody ResumeDto.TailorRequest request,
            Authentication auth) {
        String email = auth != null ? auth.getName() : null;
        return ResponseEntity.ok(resumeService.tailorResume(request, email));
    }

     // GET /api/resume/history — Get tailoring history for logged-in user
    @GetMapping("/history")
    public ResponseEntity<List<ResumeDto.ResumeHistoryResponse>> getHistory(
            Authentication auth) {
        return ResponseEntity.ok(resumeService.getHistory(auth.getName()));
    }

     // POST /api/resume/save — Save user's base resume
    @PostMapping("/save")
    public ResponseEntity<Map<String, String>> saveBaseResume(
            @Valid @RequestBody ResumeDto.UpdateResumeRequest request,
            Authentication auth) {
        resumeService.saveBaseResume(request.getResumeText(), auth.getName());
        return ResponseEntity.ok(Map.of("message", "Resume saved successfully"));
    }

     // GET /api/resume/base — Get user's saved base resume
    @GetMapping("/base")
    public ResponseEntity<Map<String, String>> getBaseResume(Authentication auth) {
        String resume = resumeService.getBaseResume(auth.getName());
        return ResponseEntity.ok(Map.of("resumeText", resume));
    }
}
