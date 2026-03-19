A full-stack career dashboard built exclusively for aspiring Java developers.
Auto-syncs live jobs, aggregates free courses, and tailors your resume with AI : all in one place.

What is JavaGo?
JavaGo solves the single biggest pain point for Java freshers — the endless, exhausting cycle of:

❌ Scrolling 25 different job portals every morning
❌ Copy-pasting resumes for every single application
❌ Finding "Java" jobs that are actually React/Angular roles in disguise

JavaGo automates all of it. It syncs real Java jobs from 5+ live platforms, aggregates free learning courses by topic, and uses AI to tailor your resume to any job description , in minutes, for free.

 Features:
1) Live Job Sync — 5 Platforms :
•Auto-fetches real Java/Spring jobs every 24 hours at 1:00 AM via scheduled sync
•Pulls from RemoteOK, YC Jobs (HackerNews), Adzuna India, Unstop, and Wellfound
•Uses both REST APIs and web scraping (Jsoup) depending on the platform
•Intelligently filters out frontend roles (React, Angular, Vue, UI Developer, etc.) even when "Java" appears in the listing
•Jobs are tagged as: LIVE, REMOTE, FRESHER, NEW for easy filtering
•Manual sync available via "Sync Jobs Now" button with real-time progress feedback

2) Free Course Aggregator
•Auto-fetches free Java learning resources from YouTube, GitHub, and freeCodeCamp
Organized by learning domain:

• Core Java  |   Advanced Java  |   Spring Boot  |   Spring AI
 SQL  |   Git & GitHub  |   AWS  |   DSA  |   Microservices

•Courses added in the last 48 hours are highlighted with a NEW badge
•Daily sync at 2:00 AM keeps the list fresh

3) AI Resume Tailor
•Paste your resume + any job description → AI rewrites your resume to match that specific JD
•Explains WHY each change was made, so you learn as you apply
•Powered by Mistral-7B via OpenRouter — completely free
•Resume history saved per user — never paste your resume twice
•Full tailoring done in under 2 minutes

4) Auth + Personal Dashboard:
•JWT-based secure login and registration
•One-click job bookmarking (⭐ Saved Jobs)
•Resume auto-saved every time you tailor or edit it
•Resume history page to revisit all past AI-tailored versions
•Per-user personalized experience

Tech Stack :

Layer                             Technology
Backend                           Java 17, Spring Boot 3.2.3
Security                          Spring Security 6.2, JWT (jjwt 0.11.5)
Database                          MySQL 8 + Spring Data JPA + Hibernate
Web Scraping                      Jsoup 1.17.2, OkHttp 4.12.0
AI Integration                    OpenRouter API (Mistral-7B free tier)
Scheduler                         Spring @Scheduled — cron-based automation
Frontend                          Vanilla JS, HTML5, CSS3 (no frameworks)
Build Tool                        Maven

How to run the project :

1) Clone the Repository : git clone https://github.com/Kunj106/javago.git
                          cd javago

 2) Set Up MySQL  : CREATE DATABASE jobapp_db;

3) Configure application.properties :
Database 
spring.datasource.url=jdbc:mysql://localhost:3306/jobapp_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=YOUR_MYSQL_PASSWORD

 JPA 
spring.jpa.hibernate.ddl-auto=update

JWT 
jwt.secret=JobAppSuperSecretKeyMustBeAtLeast256BitsLongForHMACSHA256Security2024
jwt.expiration=86400000

AI (OpenRouter — free) 
openrouter.api.key=YOUR_OPENROUTER_API_KEY
openrouter.api.url=https://openrouter.ai/api/v1/chat/completions
openrouter.model=mistralai/mistral-7b-instruct:free

 Job Sync (Adzuna India API — free)
adzuna.app.id=YOUR_ADZUNA_APP_ID
adzuna.app.key=YOUR_ADZUNA_APP_KEY

Email Digest
email.digest.to=your@gmail.com
email.digest.from=your@gmail.com
email.digest.location=mumbai
email.digest.enabled=true
email.digest.cron=0 0 6 * * *

4) Run the application :  mvn spring-boot:run

