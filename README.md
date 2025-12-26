# 🎯 Smart Resume Screener

<div align="center">

![Java](https://img.shields.io/badge/Java-17-orange?style=for-the-badge&logo=java)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.4-brightgreen?style=for-the-badge&logo=spring)
![Groq](https://img.shields.io/badge/Groq-Llama%203.3%2070B-blue?style=for-the-badge)
![License](https://img.shields.io/badge/License-MIT-yellow?style=for-the-badge)

**AI-Powered Resume Screening System with Semantic Matching**

[Features](#features) • [Architecture](#architecture) • [LLM Prompts](#llm-integration) • [Installation](#installation) • [API](#api-documentation) • [Demo](#demo)

</div>

---

## 📖 Overview

**Smart Resume Screener** is an intelligent recruitment automation tool that combines PDF parsing with Large Language Model (LLM) technology to revolutionize the hiring process. It automatically extracts structured data from resumes and uses AI to provide objective, detailed candidate evaluations.

### Key Highlights

- ⚡ **90% faster** than manual screening
- 🤖 **AI-powered** semantic matching with Groq's Llama 3.3 70B
- 📊 **Objective scoring** with detailed justifications
- 🎯 **Smart extraction** of skills, experience, and education

---

## ✨ Features

- **PDF Resume Parsing** - Automatic extraction using Apache PDFBox 3.0.3
- **AI Semantic Matching** - Context-aware evaluation beyond keywords
- **Intelligent Scoring** - 1-10 scale with detailed AI justifications
- **Modern Web UI** - Responsive interface with drag-and-drop upload
- **RESTful API** - Complete backend with Spring Boot
- **Batch Processing** - Screen multiple candidates simultaneously

---

## 🏗️ Architecture

### Architecture Diagram - Mermaid Code

```mermaid
flowchart TB
    subgraph Frontend["🖥️ PRESENTATION LAYER"]
        Browser["Web Browser<br/>HTML5 + CSS3 + JavaScript"]
    end
    
    subgraph Controller["🎮 CONTROLLER LAYER"]
        RC["ResumeController<br/>@Controller"]
        Upload["POST /api/upload"]
        Match["POST /api/match"]
        GetAll["GET /api/resumes"]
        Delete["DELETE /api/resumes"]
    end
    
    subgraph Service["⚙️ SERVICE LAYER"]
        PDF["PDFParserService<br/>📄 Parse PDF"]
        LLM["LLMMatchingService<br/>🤖 AI Matching"]
    end
    
    subgraph External["☁️ EXTERNAL SERVICES"]
        PDFBox["Apache PDFBox 3.0.3<br/>Text Extraction"]
        Groq["Groq API<br/>Llama 3.3 70B"]
    end
    
    subgraph Data["💾 DATA LAYER"]
        Repo["ResumeRepository<br/>JPA Interface"]
        DB["H2 Database<br/>In-Memory"]
    end
    
    Browser -->|HTTP/JSON| RC
    RC --> Upload
    RC --> Match
    RC --> GetAll
    RC --> Delete
    
    Upload --> PDF
    Match --> LLM
    GetAll --> Repo
    Delete --> Repo
    
    PDF --> PDFBox
    PDF --> Repo
    LLM --> Groq
    LLM --> Repo
    
    Repo --> DB
    
    style Frontend fill:#e8f4f8
    style Controller fill:#fff4e6
    style Service fill:#e8f5e9
    style External fill:#fce4ec
    style Data fill:#f3e5f5
```

### Data Flow Diagram - Mermaid Code

```mermaid
sequenceDiagram
    actor User
    participant Browser
    participant Controller as ResumeController
    participant PDFService as PDFParserService
    participant LLMService as LLMMatchingService
    participant Repository as ResumeRepository
    participant DB as H2 Database
    participant Groq as Groq API

    Note over User,Groq: RESUME UPLOAD FLOW
    
    User->>Browser: Upload PDF Resume
    Browser->>Controller: POST /api/upload
    Controller->>PDFService: parseResume(file)
    PDFService->>PDFService: Extract text with PDFBox
    PDFService->>PDFService: Parse name, email, phone
    PDFService->>PDFService: Extract skills, experience
    PDFService-->>Controller: Resume entity
    Controller->>Repository: save(resume)
    Repository->>DB: INSERT INTO resumes
    DB-->>Repository: Resume with ID
    Repository-->>Controller: Saved resume
    Controller-->>Browser: JSON response
    Browser-->>User: Success message

    Note over User,Groq: AI MATCHING FLOW
    
    User->>Browser: Enter job description
    Browser->>Controller: POST /api/match
    Controller->>Repository: findAll()
    Repository->>DB: SELECT * FROM resumes
    DB-->>Repository: List of resumes
    Repository-->>Controller: Resume list
    
    loop For each resume
        Controller->>LLMService: matchResumeWithJob()
        LLMService->>LLMService: Build prompt
        LLMService->>Groq: Send prompt to Llama 3.3
        Groq->>Groq: Semantic analysis
        Groq-->>LLMService: Score + Justification
        LLMService->>LLMService: Parse response
        LLMService-->>Controller: MatchResult
    end
    
    Controller->>Controller: Sort by score
    Controller->>Controller: Filter by threshold
    Controller-->>Browser: Ranked results
    Browser-->>User: Display matches
```

### LLM Processing Flow - Mermaid Code

```mermaid
flowchart TD
    Start([Start: Match Request]) --> Fetch[Fetch All Resumes from DB]
    Fetch --> Loop{For Each Resume}
    
    Loop -->|Next Resume| Build[Build Structured Prompt]
    
    Build --> Prompt["Prompt Template:<br/>- Job Description<br/>- Candidate Skills<br/>- Experience<br/>- Education<br/>- Scoring Instructions"]
    
    Prompt --> Send[Send to Groq API]
    Send --> Groq["Groq Cloud<br/>Llama 3.3 70B<br/>Temperature: 0.3"]
    
    Groq --> Response["AI Response:<br/>Score: 8.5<br/>Justification: ..."]
    
    Response --> Parse[Parse Response with Regex]
    Parse --> Extract["Extract:<br/>- Match Score<br/>- Justification Text"]
    
    Extract --> Create[Create MatchResult Object]
    Create --> Store[Add to Results List]
    
    Store --> Loop
    Loop -->|All Done| Sort[Sort by Score DESC]
    
    Sort --> Filter[Filter by Threshold >= 6.0]
    Filter --> Return[Return JSON Response]
    Return --> End([End: Display Results])
    
    style Start fill:#4CAF50,color:#fff
    style End fill:#4CAF50,color:#fff
    style Groq fill:#2196F3,color:#fff
    style Response fill:#FF9800,color:#fff
    style Filter fill:#9C27B0,color:#fff
```

***


## 🤖 LLM Integration

### Prompt Engineering Strategy

Our system uses a carefully engineered prompt template to ensure consistent, high-quality AI evaluations.

### Complete Prompt Template

private String buildMatchingPrompt(Resume resume, String jobDescription) {
return """
You are an expert technical recruiter. Analyze the candidate's
resume against the job description and provide an objective assessment.


    ══════════════════════════════════════════════════════════════
    JOB DESCRIPTION:
    ══════════════════════════════════════════════════════════════
    %s
    
    ══════════════════════════════════════════════════════════════
    CANDIDATE RESUME:
    ══════════════════════════════════════════════════════════════
    Name: %s
    Email: %s
    Phone: %s
    Skills: %s
    Experience: %s
    Education: %s
    
    ══════════════════════════════════════════════════════════════
    EVALUATION CRITERIA:
    ══════════════════════════════════════════════════════════════
    
    1. TECHNICAL SKILLS (40%%)
       - Required technologies and frameworks match
       - Skill level and depth of expertise
       - Critical gaps identification
    
    2. EXPERIENCE (30%%)
       - Years of relevant experience
       - Similar project work
       - Domain knowledge alignment
    
    3. EDUCATION (20%%)
       - Educational background match
       - Relevant degrees and certifications
    
    4. OVERALL FIT (10%%)
       - Career progression
       - Special achievements
       - Growth potential
    
    ══════════════════════════════════════════════════════════════
    SCORING SCALE:
    ══════════════════════════════════════════════════════════════
    
    9-10: Exceptional fit - Highly recommended
    7-8:  Strong fit - Recommended for interview
    5-6:  Moderate fit - Consider with reservations
    3-4:  Weak fit - Missing key requirements
    1-2:  Poor fit - Not recommended
    
    ══════════════════════════════════════════════════════════════
    RESPONSE FORMAT (MUST FOLLOW):
    ══════════════════════════════════════════════════════════════
    
    Score: [number 1-10]
    
    Justification: [3-4 sentences explaining the score, highlighting 
    strengths, identifying gaps, and providing specific reasoning]
    
    """.formatted(jobDescription, 
                 resume.getCandidateName(),
                 resume.getEmail(),
                 resume.getPhone(),
                 resume.getSkills(),
                 resume.getExperience(),
                 resume.getEducation());
}


### LLM Configuration

Groq API Configuration
spring.ai.openai.api-key=gsk_YOUR_GROQ_API_KEY
spring.ai.openai.base-url=https://api.groq.com/openai
spring.ai.openai.chat.options.model=llama-3.3-70b-versatile
spring.ai.openai.chat.options.temperature=0.3
spring.ai.openai.chat.options.max-tokens=1000



### Why This Works

| Aspect | Implementation | Benefit |
|--------|---------------|---------|
| **Role Definition** | "Expert technical recruiter" | Primes model for recruitment context |
| **Structured Input** | Organized sections with separators | Improves parsing and comprehension |
| **Weighted Criteria** | 40% skills, 30% experience, etc. | Ensures balanced evaluation |
| **Scoring Anchors** | Descriptive scale (9-10 = Exceptional) | Prevents score inflation |
| **Format Enforcement** | "Score: X\nJustification: Y" | Enables reliable regex parsing |
| **Low Temperature** | 0.3 for consistency | Reduces randomness across candidates |

### Example Response

**Input:**
Job: "Senior Java Developer, 5+ years Spring Boot"
Candidate: "7 years Java, Spring Boot, Microservices, AWS"



**AI Output:**
Score: 8.5

Justification: The candidate demonstrates exceptional alignment with
7 years of Java experience exceeding the 5+ year requirement, plus
hands-on Spring Boot and microservices expertise matching core needs.
AWS cloud experience adds significant value for modern deployments.
Strong recommendation for technical interview to verify project depth.



---

## 🛠️ Technology Stack

| Layer | Technology | Version | Purpose |
|-------|-----------|---------|---------|
| **Backend** | Java | 17 | Core language |
| | Spring Boot | 3.3.4 | Application framework |
| | Spring Data JPA | 3.3.4 | Data persistence |
| | Hibernate | 6.5.3 | ORM |
| **AI** | Spring AI | 1.0.0-M3 | LLM integration |
| | Groq API | Latest | LLM provider |
| | Llama 3.3 70B | Latest | Language model |
| **PDF** | Apache PDFBox | 3.0.3 | PDF parsing |
| **Database** | H2 | 2.x | In-memory DB |
| **Frontend** | HTML/CSS/JS | ES6 | Web interface |
| **Build** | Maven | 3.9+ | Build tool |

---

## 🚀 Installation

### Prerequisites

- ☕ Java 17+ ([Download](https://www.oracle.com/java/technologies/downloads/))
- 📦 Maven 3.6+ ([Download](https://maven.apache.org/download.cgi))
- 🔑 Groq API Key ([Free Signup](https://console.groq.com))

### Quick Start

1. Clone repository
git clone https://github.com/YOUR_USERNAME/smart-resume-screener.git
cd smart-resume-screener

2. Configure API key in src/main/resources/application.properties
Replace: spring.ai.openai.api-key=gsk_YOUR_KEY
3. Build and run
mvn clean install
mvn spring-boot:run

4. Access at http://localhost:8080


### Detailed Setup

**Step 1: Get Groq API Key**
1. Visit https://console.groq.com
2. Sign up (free)
3. Go to "API Keys"
4. Create new key
5. Copy key (starts with `gsk_`)

**Step 2: Configure Application**

Edit `src/main/resources/application.properties`:
spring.ai.openai.api-key=gsk_YOUR_ACTUAL_KEY_HERE
spring.ai.openai.base-url=https://api.groq.com/openai
spring.ai.openai.chat.options.model=llama-3.3-70b-versatile



**Step 3: Run Application**
mvn spring-boot:run



---

## 📖 API Documentation

### Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/upload` | Upload PDF resume |
| `POST` | `/api/match` | Match resumes with job |
| `GET` | `/api/resumes` | Get all resumes |
| `DELETE` | `/api/resumes/{id}` | Delete specific resume |
| `DELETE` | `/api/resumes` | Delete all resumes |

### Example: Upload Resume

**Request:**
curl -X POST http://localhost:8080/api/upload
-F "file=@resume.pdf"



**Response:**
{
"success": true,
"resumeId": 1,
"candidateName": "Sukesh",
"data": {
"id": 1,
"candidateName": "Sukesh",
"email": "Sukesh@gmail.com",
"phone": "+91-8121865233",
"skills": "Java, Spring Boot, MySQL, REST APIs",
"experience": "3 years as Java Developer at TechCorp",
"education": "B.Tech Computer Science, VIT"
}
}


### Example: Match Resumes

**Request:**
curl -X POST http://localhost:8080/api/match
-H "Content-Type: application/json"
-d '{
"jobDescription": "Senior Java Developer with 5+ years experience in Spring Boot, microservices, and cloud technologies. Must have strong knowledge of REST APIs, MySQL, Docker, and AWS.",
"threshold": 6.0
}'



**Response:**
{
"success": true,
"totalCandidates": 5,
"shortlistedCount": 3,
"results": [
{
"resumeId": 1,
"candidateName": "Sukesh",
"matchScore": 8.5,
"justification": "Strong match for the Senior Java Developer position. The candidate has 3 years of hands-on experience with Java and Spring Boot, which aligns well with the technical requirements. Skills in REST APIs and MySQL demonstrate full-stack capabilities. However, the candidate has only 3 years of experience while the job requires 5+, and there's no mention of cloud technologies like AWS or Docker. Overall, a solid candidate who could grow into the role with some upskilling in cloud platforms.",
"skills": "Java, Spring Boot, MySQL, REST APIs",
"experience": "3 years as Java Developer at TechCorp",
"education": "B.Tech Computer Science, NIT",
"email": "Sukesh@gmail.com",
"phone": "+91-8121865233"
},
{
"resumeId": 2,
"candidateName": "Priya Sharma",
"matchScore": 7.0,
"justification": "Good match with some gaps. The candidate has 2 years of experience with Java and Spring Framework. Strong educational background. Missing cloud experience and microservices knowledge. Would be suitable for a mid-level position.",
"skills": "Java, Spring Framework, MySQL",
"experience": "2 years as Backend Developer",
"education": "B.E. Computer Science, Mumbai University",
"email": "priya.sharma@gmail.com",
"phone": "+91-9988776655"
},
{
"resumeId": 3,
"candidateName": "Amit Kumar",
"matchScore": 6.5,
"justification": "Moderate match. Fresh graduate with strong academic foundation. Knowledge of Core Java and Spring Boot through internship and projects. Limited professional experience but high potential for growth.",
"skills": "Core Java, Spring Boot, MySQL, Git",
"experience": "6 months internship at ABC Tech",
"education": "B.Tech Computer Science, IIT Delhi",
"email": "amit.kumar@example.com",
"phone": "+91-9123456789"
}
]
}



### Example: Get All Resumes

**Request:**
curl -X GET http://localhost:8080/api/resumes



**Response:**
[
{
"id": 1,
"candidateName": "Sukesh",
"email": "Sukesh@gmail.com",
"phone": "+91-8121865233",
"skills": "Java, Spring Boot, MySQL",
"experience": "3 years as Java Developer",
"education": "B.Tech Computer Science",
"uploadedAt": "2025-10-13T13:45:30"
},
{
"id": 2,
"candidateName": "Priya Sharma",
"email": "priya.sharma@gmail.com",
"phone": "+91-9988776655",
"skills": "Java, Spring Framework, MySQL",
"experience": "2 years as Backend Developer",
"education": "B.E. Computer Science",
"uploadedAt": "2025-10-13T14:10:15"
}
]



### Example: Delete Resume

**Request:**
curl -X DELETE http://localhost:8080/api/resumes/1



**Response:**
{
"success": true,
"message": "Resume deleted successfully"
}



### Example: Delete All Resumes

**Request:**
curl -X DELETE http://localhost:8080/api/resumes



**Response:**
{
"success": true,
"message": "All resumes deleted successfully"
}


undefined


---

## 📁 Project Structure

```
smart-resume-screener/
├── src/
│   ├── main/
│   │   ├── java/com/resumescreener/
│   │   │   ├── SmartResumeScreenerApplication.java
│   │   │   ├── controller/
│   │   │   │   └── ResumeController.java
│   │   │   ├── service/
│   │   │   │   ├── PDFParserService.java
│   │   │   │   └── LLMMatchingService.java
│   │   │   ├── model/
│   │   │   │   ├── Resume.java
│   │   │   │   └── MatchResult.java
│   │   │   └── repository/
│   │   │       └── ResumeRepository.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── templates/
│   │           └── index.html
│   └── test/
├── pom.xml
├── README.md
└── .gitignore
```




---

## 📊 Performance

- **PDF Processing**: < 2 seconds per resume
- **AI Matching**: 3-5 seconds per candidate
- **API Response**: < 500ms (excluding AI)
- **Throughput**: 30 requests/minute (Groq free tier)

---

## 🎬 Demo

### Screenshots

**Landing Page:**
![Landing Page](pics/1.jpeg)

**Upload Interface and AI Results:**
![Upload Interface](pics/3.jpeg)



---

## 🔐 Security

- ✅ API keys in properties (excluded from Git)
- ✅ Input validation (PDF only, 10MB limit)
- ✅ SQL injection prevention (JPA)
- ✅ XSS protection (Thymeleaf escaping)

---

## 🚧 Future Enhancements

- [ ] DOCX resume support
- [ ] Email notifications
- [ ] Export to Excel/CSV
- [ ] User authentication
- [ ] PostgreSQL for production
- [ ] Docker deployment

---

## 👨‍💻 Author

**Abhishek B**
- GitHub: [@abhishekreddy16](https://github.com/abhishekreddy16)
- Email: abhishek16reddy@gmail.com

---

## 🙏 Acknowledgments

- Spring AI Team - AI integration framework
- Groq - Free LLM API access
- Apache PDFBox - PDF parsing library

---


<div align="center">

**⭐ Star this repo if you find it useful! ⭐**


</div>
