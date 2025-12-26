package com.resumescreener.model;

public class MatchResult {
    
    private Long resumeId;
    private String candidateName;
    private Double matchScore;
    private String justification;
    private String skills;
    private String experience;
    private String education;
    private String email;
    private String phone;
    
    public MatchResult() {
    }
    
    public MatchResult(Resume resume, Double matchScore, String justification) {
        this.resumeId = resume.getId();
        this.candidateName = resume.getCandidateName();
        this.matchScore = matchScore;
        this.justification = justification;
        this.skills = resume.getSkills();
        this.experience = resume.getExperience();
        this.education = resume.getEducation();
        this.email = resume.getEmail();
        this.phone = resume.getPhone();
    }
    
    // Getters and Setters
    public Long getResumeId() { return resumeId; }
    public void setResumeId(Long resumeId) { this.resumeId = resumeId; }
    
    public String getCandidateName() { return candidateName; }
    public void setCandidateName(String candidateName) { this.candidateName = candidateName; }
    
    public Double getMatchScore() { return matchScore; }
    public void setMatchScore(Double matchScore) { this.matchScore = matchScore; }
    
    public String getJustification() { return justification; }
    public void setJustification(String justification) { this.justification = justification; }
    
    public String getSkills() { return skills; }
    public void setSkills(String skills) { this.skills = skills; }
    
    public String getExperience() { return experience; }
    public void setExperience(String experience) { this.experience = experience; }
    
    public String getEducation() { return education; }
    public void setEducation(String education) { this.education = education; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}
