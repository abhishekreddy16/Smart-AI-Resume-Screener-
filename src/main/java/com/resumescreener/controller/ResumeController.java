package com.resumescreener.controller;

import com.resumescreener.model.MatchResult;
import com.resumescreener.model.Resume;
import com.resumescreener.repository.ResumeRepository;
import com.resumescreener.service.LLMMatchingService;
import com.resumescreener.service.PDFParserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class ResumeController {
    
    @Autowired
    private PDFParserService pdfParserService;
    
    @Autowired
    private LLMMatchingService llmMatchingService;
    
    @Autowired
    private ResumeRepository resumeRepository;
    
    @GetMapping("/")
    public String home(Model model) {
        List<Resume> resumes = resumeRepository.findAll();
        model.addAttribute("resumeCount", resumes.size());
        return "index";
    }
    
    @PostMapping("/api/upload")
    @ResponseBody
    public ResponseEntity<?> uploadResume(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (file.isEmpty()) {
                response.put("success", false);
                response.put("error", "Please select a file to upload");
                return ResponseEntity.badRequest().body(response);
            }
            
            String filename = file.getOriginalFilename();
            if (filename == null || !filename.toLowerCase().endsWith(".pdf")) {
                response.put("success", false);
                response.put("error", "Only PDF files are supported");
                return ResponseEntity.badRequest().body(response);
            }
            
            Resume resume = pdfParserService.parseResume(file);
            resume = resumeRepository.save(resume);
            
            response.put("success", true);
            response.put("message", "Resume uploaded successfully");
            response.put("resumeId", resume.getId());
            response.put("candidateName", resume.getCandidateName());
            response.put("data", resume);
            
            return ResponseEntity.ok(response);
            
        } catch (IOException e) {
            response.put("success", false);
            response.put("error", "Error processing resume: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Unexpected error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @PostMapping("/api/match")
    @ResponseBody
    public ResponseEntity<?> matchResumes(@RequestBody MatchRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Validate input
            if (request.getJobDescription() == null || request.getJobDescription().trim().isEmpty()) {
                response.put("success", false);
                response.put("error", "Job description is required");
                return ResponseEntity.badRequest().body(response);
            }
            
            List<Resume> resumes = resumeRepository.findAll();
            
            if (resumes.isEmpty()) {
                response.put("success", false);
                response.put("error", "No resumes found. Please upload resumes first.");
                return ResponseEntity.badRequest().body(response);
            }
            
            List<MatchResult> results = new ArrayList<>();
            
            // Process each resume
            for (Resume resume : resumes) {
                try {
                    MatchResult matchResult = llmMatchingService.matchResumeWithJob(
                            resume, 
                            request.getJobDescription()
                    );
                    results.add(matchResult);
                } catch (Exception e) {
                    System.err.println("Error matching resume " + resume.getId() + ": " + e.getMessage());
                    e.printStackTrace();
                    
                    // Add a fallback result
                    MatchResult fallbackResult = new MatchResult();
                    fallbackResult.setResumeId(resume.getId());
                    fallbackResult.setCandidateName(resume.getCandidateName());
                    fallbackResult.setMatchScore(0.0);
                    fallbackResult.setJustification("Error during matching: " + e.getMessage());
                    fallbackResult.setSkills(resume.getSkills());
                    fallbackResult.setExperience(resume.getExperience());
                    fallbackResult.setEducation(resume.getEducation());
                    fallbackResult.setEmail(resume.getEmail());
                    fallbackResult.setPhone(resume.getPhone());
                    results.add(fallbackResult);
                }
            }
            
            // Sort by match score descending
            results.sort(Comparator.comparing(MatchResult::getMatchScore).reversed());
            
            // Filter by threshold
            double threshold = request.getThreshold() != null ? request.getThreshold() : 6.0;
            List<MatchResult> shortlisted = results.stream()
                    .filter(r -> r.getMatchScore() >= threshold)
                    .collect(Collectors.toList());
            
            response.put("success", true);
            response.put("totalCandidates", results.size());
            response.put("shortlistedCount", shortlisted.size());
            response.put("results", shortlisted);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("error", "Error matching resumes: " + e.getMessage());
            response.put("details", e.getClass().getName());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @GetMapping("/api/resumes")
    @ResponseBody
    public ResponseEntity<?> getAllResumes() {
        try {
            List<Resume> resumes = resumeRepository.findAll();
            return ResponseEntity.ok(resumes);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @DeleteMapping("/api/resumes/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteResume(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            resumeRepository.deleteById(id);
            response.put("success", true);
            response.put("message", "Resume deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @DeleteMapping("/api/resumes")
    @ResponseBody
    public ResponseEntity<?> deleteAllResumes() {
        Map<String, Object> response = new HashMap<>();
        try {
            resumeRepository.deleteAll();
            response.put("success", true);
            response.put("message", "All resumes deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    // Inner class for request body
    public static class MatchRequest {
        private String jobDescription;
        private Double threshold;
        
        public String getJobDescription() {
            return jobDescription;
        }
        
        public void setJobDescription(String jobDescription) {
            this.jobDescription = jobDescription;
        }
        
        public Double getThreshold() {
            return threshold;
        }
        
        public void setThreshold(Double threshold) {
            this.threshold = threshold;
        }
    }
}
