package com.resumescreener.service;

import com.resumescreener.model.MatchResult;
import com.resumescreener.model.Resume;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class LLMMatchingService {
    
    private final ChatClient chatClient;
    
    @Autowired
    public LLMMatchingService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }
    
    public MatchResult matchResumeWithJob(Resume resume, String jobDescription) {
        try {
            String prompt = buildMatchingPrompt(resume, jobDescription);
            
            System.out.println("Sending prompt to OpenAI...");
            
            String llmResponse = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();
            
            System.out.println("Received response: " + llmResponse);
            
            Double matchScore = extractScore(llmResponse);
            String justification = extractJustification(llmResponse);
            
            return new MatchResult(resume, matchScore, justification);
            
        } catch (Exception e) {
            System.err.println("Error in LLM matching: " + e.getMessage());
            e.printStackTrace();
            
            // Return fallback result
            return new MatchResult(resume, 0.0, "Error: " + e.getMessage());
        }
    }
    
    private String buildMatchingPrompt(Resume resume, String jobDescription) {
        String template = """
                You are an expert resume screening AI assistant. Analyze the following resume against the job description.
                
                RESUME DETAILS:
                Candidate Name: {candidateName}
                Skills: {skills}
                Experience: {experience}
                Education: {education}
                
                JOB DESCRIPTION:
                {jobDescription}
                
                INSTRUCTIONS:
                1. Compare the candidate's skills, experience, and education with the job requirements.
                2. Rate the fit on a scale of 1-10 (where 10 is perfect match).
                3. Provide a detailed justification for the score.
                
                RESPONSE FORMAT:
                Score: [number between 1-10]
                Justification: [detailed explanation covering skills match, experience relevance, education fit, strengths, and gaps]
                
                Now analyze and respond:
                """;
        
        PromptTemplate promptTemplate = new PromptTemplate(template);
        Prompt prompt = promptTemplate.create(Map.of(
                "candidateName", resume.getCandidateName(),
                "skills", resume.getSkills(),
                "experience", resume.getExperience(),
                "education", resume.getEducation(),
                "jobDescription", jobDescription
        ));
        
        return prompt.getContents();
    }
    
    private Double extractScore(String llmResponse) {
        // Extract score from response
        Pattern scorePattern = Pattern.compile("Score:\\s*(\\d+\\.?\\d*)");
        Matcher matcher = scorePattern.matcher(llmResponse);
        
        if (matcher.find()) {
            try {
                return Double.parseDouble(matcher.group(1));
            } catch (NumberFormatException e) {
                return 5.0; // Default score
            }
        }
        
        // Fallback: look for any number between 1-10
        Pattern fallbackPattern = Pattern.compile("(\\d+\\.?\\d*)/10|rating.*?(\\d+\\.?\\d*)");
        matcher = fallbackPattern.matcher(llmResponse);
        if (matcher.find()) {
            String scoreStr = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
            try {
                return Double.parseDouble(scoreStr);
            } catch (NumberFormatException e) {
                return 5.0;
            }
        }
        
        return 5.0; // Default fallback
    }
    
    private String extractJustification(String llmResponse) {
        // Extract justification from response
        Pattern justificationPattern = Pattern.compile("Justification:\\s*(.+)", Pattern.DOTALL);
        Matcher matcher = justificationPattern.matcher(llmResponse);
        
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        
        // If no explicit justification section, return the whole response
        return llmResponse;
    }
}
