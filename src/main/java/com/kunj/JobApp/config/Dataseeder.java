package com.kunj.JobApp.config;

import com.kunj.JobApp.entity.*;
import com.kunj.JobApp.repository.FreeCourseRepository;
import com.kunj.JobApp.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class Dataseeder implements CommandLineRunner
{
    private final JobRepository jobRepository;
    private final FreeCourseRepository courseRepository;

    @Override
    public void run(String... args) {
        seedJobsIfEmpty();
        seedCoursesIfEmpty();
    }

    //JOBS
    private void seedJobsIfEmpty() {
        if (jobRepository.count() > 0) return;
        log.info("Seeding job data...");

        jobRepository.saveAll(List.of(

                // MNC
                job("Java Developer - Fresher", "Infosys", "Mumbai", JobType.JOB, CompanyType.MNC,
                        "Join our Java team as a fresher and work on enterprise-grade applications.",
                        "B.E/B.Tech in CS/IT, Core Java, OOP Concepts",
                        "3.5 - 4.5 LPA", "https://infosys.com/careers",
                        "0-1 years", "Java, OOP, SQL, Git"),

                job("Junior Java Developer", "TCS", "Mumbai", JobType.JOB, CompanyType.MNC,
                        "Work on Java-based microservices for global banking clients.",
                        "Java, Spring Boot basics, REST APIs",
                        "3.5 - 5 LPA", "https://tcs.com/careers",
                        "0-1 years", "Java, Spring Boot, REST, MySQL"),

                job("Software Engineer Trainee - Java", "Wipro", "Mumbai", JobType.JOB, CompanyType.MNC,
                        "Trainee role for Java developers to work on product engineering.",
                        "Core Java, JDBC, SQL",
                        "3.5 LPA", "https://wipro.com/careers",
                        "Fresher", "Core Java, JDBC, SQL, OOP"),

                job("Associate Software Engineer - Java", "Accenture", "Mumbai", JobType.JOB, CompanyType.MNC,
                        "Java backend development for financial services projects.",
                        "Java 8+, Collections, Exception Handling",
                        "4 - 5 LPA", "https://accenture.com/in-en/careers",
                        "0-1 years", "Java 8, Spring, Hibernate, SQL"),

                job("Java Developer", "Cognizant", "Mumbai", JobType.JOB, CompanyType.MNC,
                        "Work on Java enterprise applications for global clients.",
                        "Java, Spring MVC, Hibernate, MySQL",
                        "4 - 5.5 LPA", "https://cognizant.com/in/en/careers",
                        "0-2 years", "Java, Spring MVC, Hibernate, MySQL"),

                // STARTUP
                job("Backend Java Developer - Fresher", "Razorpay", "Mumbai", JobType.JOB, CompanyType.STARTUP,
                        "Build payment infrastructure using Java microservices.",
                        "Java, Spring Boot, REST, Basic DSA",
                        "6 - 10 LPA", "https://razorpay.com/jobs",
                        "0-1 years", "Java, Spring Boot, Kafka, Redis"),

                job("Java Backend Engineer", "Zerodha", "Mumbai", JobType.JOB, CompanyType.STARTUP,
                        "Work on trading platform backend. High-performance Java systems.",
                        "Java, multithreading, low-latency systems basics",
                        "7 - 12 LPA", "https://zerodha.com/careers",
                        "0-2 years", "Java, Concurrency, PostgreSQL, Linux"),

                job("Java Developer - Fresher", "Groww", "Mumbai", JobType.JOB, CompanyType.STARTUP,
                        "Build features for India's fastest-growing investment platform.",
                        "Java, Spring Boot, Microservices basics",
                        "8 - 12 LPA", "https://groww.in/jobs",
                        "0-1 years", "Java, Spring Boot, AWS, MySQL"),

                job("Software Developer - Java", "CRED", "Mumbai", JobType.JOB, CompanyType.STARTUP,
                        "Backend development for credit card management platform.",
                        "Java, Spring Boot, REST APIs, SQL",
                        "10 - 15 LPA", "https://careers.cred.club",
                        "0-2 years", "Java, Spring Boot, Microservices, AWS"),

                // MIDSIZE
                job("Junior Java Developer", "Mphasis", "Mumbai", JobType.JOB, CompanyType.MIDSIZE,
                        "Java development for BFSI domain clients.",
                        "Core Java, Spring, Hibernate",
                        "4 - 6 LPA", "https://mphasis.com/home/careers",
                        "0-2 years", "Java, Spring, Hibernate, Oracle DB"),

                job("Java Developer Fresher", "Hexaware", "Mumbai", JobType.JOB, CompanyType.MIDSIZE,
                        "BFSI-focused Java development and support.",
                        "Java, SQL, Basic Spring",
                        "3.5 - 5 LPA", "https://hexaware.com/careers",
                        "Fresher", "Java, SQL, Spring, Git"),

                job("Associate Developer - Java", "Persistent Systems", "Mumbai", JobType.JOB, CompanyType.MIDSIZE,
                        "Product development using Java and cloud technologies.",
                        "Java, Spring Boot, REST APIs",
                        "5 - 7 LPA", "https://persistent.com/careers",
                        "0-2 years", "Java, Spring Boot, AWS, Docker"),

                // FINTECH
                job("Java Backend Developer", "PhonePe", "Mumbai", JobType.JOB, CompanyType.FINTECH,
                        "Build scalable payment processing APIs.",
                        "Java, Spring Boot, Microservices, SQL",
                        "8 - 14 LPA", "https://phonepe.com/en/careers",
                        "0-2 years", "Java, Spring Boot, Kafka, MySQL"),

                job("Java Developer - Fresher", "Paytm", "Mumbai", JobType.JOB, CompanyType.FINTECH,
                        "Payments platform backend development role.",
                        "Core Java, Collections, Spring basics",
                        "6 - 10 LPA", "https://paytm.com/careers",
                        "0-1 years", "Java, Spring Boot, Redis, MySQL"),

                job("Software Engineer - Java", "BharatPe", "Mumbai", JobType.JOB, CompanyType.FINTECH,
                        "Build merchant payment infrastructure.",
                        "Java 8+, Spring Boot, REST",
                        "7 - 11 LPA", "https://bharatpe.com/careers",
                        "0-2 years", "Java, Spring Boot, MongoDB, AWS"),

                // BANKING
                job("Associate Software Engineer - Java", "HDFC Bank", "Mumbai", JobType.JOB, CompanyType.BANKING,
                        "Technology division Java developer for core banking systems.",
                        "Java, Spring, SQL, Banking domain basics",
                        "5 - 8 LPA", "https://hdfcbank.com/content/bbp/repositories/723fb80a-2dde-42a3-9793-7ae1be57c87f/?folderPath=/footer/Careers",
                        "0-2 years", "Java, Spring, SQL, Oracle"),

                job("Junior Software Developer - Java", "ICICI Bank", "Mumbai", JobType.JOB, CompanyType.BANKING,
                        "Digital banking product development using Java.",
                        "Core Java, Spring Boot, Microservices",
                        "5 - 8 LPA", "https://www.icicibankjobs.com",
                        "0-2 years", "Java, Spring Boot, SQL, REST APIs"),

                // INTERNSHIPS
                job("Java Developer Intern", "Infosys SpringBoard", "Mumbai", JobType.INTERNSHIP, CompanyType.MNC,
                        "6-month internship for Java development with possible full-time offer.",
                        "Core Java, OOP, Basic SQL",
                        "15,000 - 25,000/month", "https://infosys.com/careers/springboard",
                        "Fresher/Student", "Java, SQL, OOP, Git"),

                job("Backend Java Intern", "Razorpay", "Mumbai", JobType.INTERNSHIP, CompanyType.STARTUP,
                        "3-month paid internship on payment backend systems.",
                        "Java, Spring Boot basics, REST",
                        "25,000 - 40,000/month", "https://razorpay.com/jobs",
                        "Student/Fresher", "Java, Spring Boot, REST, MySQL"),

                job("Java Development Intern", "Mphasis", "Mumbai", JobType.INTERNSHIP, CompanyType.MIDSIZE,
                        "Summer internship for Java enthusiasts.",
                        "Core Java, JDBC, Basic SQL",
                        "10,000 - 20,000/month", "https://mphasis.com/home/careers",
                        "Student/Fresher", "Java, JDBC, SQL"),

                job("Software Development Intern - Java", "HDFC Bank Tech", "Mumbai", JobType.INTERNSHIP, CompanyType.BANKING,
                        "Internship in digital banking technology team.",
                        "Java, Spring basics, SQL",
                        "20,000 - 30,000/month", "https://hdfcbank.com/careers",
                        "Student/Fresher", "Java, SQL, Spring"),

                job("Java Intern - Fintech", "PhonePe", "Mumbai", JobType.INTERNSHIP, CompanyType.FINTECH,
                        "6-month paid internship with PPO opportunity.",
                        "Java, REST APIs, Basic Spring Boot",
                        "30,000 - 50,000/month", "https://phonepe.com/en/careers",
                        "Student/Fresher", "Java, Spring Boot, REST, SQL")
        ));
        log.info("✅ Seeded {} jobs", jobRepository.count());
    }

    //COURSES

    private void seedCoursesIfEmpty() {
        if (courseRepository.count() > 0) return;
        log.info("Seeding courses data...");

        courseRepository.saveAll(List.of(

                // CORE JAVA
                course("Java Tutorial for Beginners - Full Course", "YouTube - Programming with Mosh", "Core Java",
                        "Complete Java programming course from scratch covering OOP, collections, exception handling.",
                        "Programming with Mosh", "5 hours", "Beginner",
                        "https://www.youtube.com/watch?v=eIrMbAQSU34",
                        CourseCategory.CORE_JAVA),

                course("Core Java Full Course - Java Tutorial", "YouTube - Telusko", "Core Java",
                        "Comprehensive Java course covering all core concepts with real examples.",
                        "Navin Reddy", "52 hours", "Beginner",
                        "https://www.youtube.com/watch?v=BGTx91t8q50",
                        CourseCategory.CORE_JAVA),

                course("Java Programming - NPTEL", "NPTEL", "Core Java",
                        "IIT-level Java programming course with assignments and certification.",
                        "IIT Faculty", "40 hours", "Beginner",
                        "https://nptel.ac.in/courses/106105191",
                        CourseCategory.CORE_JAVA),

                course("Java Full Course for Beginners", "YouTube - Bro Code", "Core Java",
                        "Beginner-friendly Java tutorial with 100+ mini-projects.",
                        "Bro Code", "12 hours", "Beginner",
                        "https://www.youtube.com/watch?v=xk4_1vDrzzo",
                        CourseCategory.CORE_JAVA),

                // ADVANCED JAVA
                course("Advanced Java Full Course", "YouTube - Telusko", "Advanced Java",
                        "Servlet, JSP, JDBC, Hibernate, Spring MVC all in one course.",
                        "Navin Reddy", "30 hours", "Advanced",
                        "https://www.youtube.com/watch?v=Ae-r8hsbPUo",
                        CourseCategory.ADVANCED_JAVA),

                course("Java Multithreading and Concurrency", "YouTube - Java Brains", "Advanced Java",
                        "In-depth multithreading, executors, concurrent collections.",
                        "Koushik Kothagal", "8 hours", "Advanced",
                        "https://www.youtube.com/c/JavaBrainsChannel",
                        CourseCategory.ADVANCED_JAVA),

                course("Java Design Patterns", "YouTube - Derek Banas", "Advanced Java",
                        "All 23 Gang of Four design patterns explained with Java code.",
                        "Derek Banas", "6 hours", "Advanced",
                        "https://www.youtube.com/watch?v=vNHpsC5ng_E",
                        CourseCategory.ADVANCED_JAVA),

                // SPRING BOOT
                course("Spring Boot Tutorial - Full Course", "YouTube - Amigoscode", "Spring Boot",
                        "Spring Boot 3 full course with REST APIs, JPA, Security, and Docker.",
                        "Nelson Djalo", "3.5 hours", "Intermediate",
                        "https://www.youtube.com/watch?v=9SGDpanrc8U",
                        CourseCategory.SPRING_BOOT),

                course("Spring Boot 3 Full Course 2024", "YouTube - Daily Code Buffer", "Spring Boot",
                        "Spring Boot 3 with Spring Security 6, JWT, Microservices.",
                        "Shabbir Dawoodi", "10 hours", "Intermediate",
                        "https://www.youtube.com/watch?v=Nv2DERaMx-4",
                        CourseCategory.SPRING_BOOT),

                course("Spring Boot Microservices", "YouTube - Java Techie", "Spring Boot",
                        "Build production-ready microservices with Spring Boot and Docker.",
                        "Java Techie", "15 hours", "Intermediate",
                        "https://www.youtube.com/c/JavaTechie",
                        CourseCategory.SPRING_BOOT),

                course("Spring Framework - Spring.io Guides", "Spring.io Official", "Spring Boot",
                        "Official Spring guides for Boot, Security, Data JPA, Cloud, and more.",
                        "Spring Team", "Self-paced", "Beginner",
                        "https://spring.io/guides",
                        CourseCategory.SPRING_BOOT),

                // SPRING AI
                course("Spring AI Tutorial", "YouTube - Dan Vega", "Spring AI",
                        "Introduction to Spring AI - chatbots, embeddings, RAG with Java.",
                        "Dan Vega", "4 hours", "Intermediate",
                        "https://www.youtube.com/c/DanVega",
                        CourseCategory.SPRING_AI),

                course("Spring AI - Official Documentation", "Spring.io", "Spring AI",
                        "Official reference documentation for Spring AI framework.",
                        "Spring Team", "Self-paced", "Intermediate",
                        "https://docs.spring.io/spring-ai/reference/",
                        CourseCategory.SPRING_AI),

                // SQL
                course("SQL Full Course - MySQL Tutorial", "YouTube - Programming with Mosh", "SQL",
                        "MySQL database fundamentals - queries, joins, indexes, stored procedures.",
                        "Programming with Mosh", "3 hours", "Beginner",
                        "https://www.youtube.com/watch?v=7S_tz1z_5bA",
                        CourseCategory.SQL_DATABASE),

                course("MySQL Full Course for Free", "YouTube - Bro Code", "SQL",
                        "Complete MySQL course covering DDL, DML, joins, views, and more.",
                        "Bro Code", "4 hours", "Beginner",
                        "https://www.youtube.com/watch?v=5OdVJbNCSso",
                        CourseCategory.SQL_DATABASE),

                course("SQL Tutorial - Mode Analytics", "Mode.com", "SQL",
                        "Hands-on SQL tutorials with a browser-based SQL environment.",
                        "Mode Team", "Self-paced", "Beginner",
                        "https://mode.com/sql-tutorial/",
                        CourseCategory.SQL_DATABASE),

                course("SQLZoo - Interactive SQL Tutorial", "SQLZoo", "SQL",
                        "Interactive SQL exercises from beginner to advanced.",
                        "SQLZoo", "Self-paced", "Beginner",
                        "https://sqlzoo.net/wiki/SQL_Tutorial",
                        CourseCategory.SQL_DATABASE),

                // GIT & GITHUB
                course("Git and GitHub Full Course", "YouTube - Apna College", "Git & GitHub",
                        "Complete Git + GitHub tutorial in Hindi for beginners.",
                        "Shradha Khapra", "5 hours", "Beginner",
                        "https://www.youtube.com/watch?v=Ez8F0nW6S-w",
                        CourseCategory.GIT_GITHUB),

                course("Git Tutorial for Beginners", "YouTube - Programming with Mosh", "Git & GitHub",
                        "Learn Git in 1 hour - all essential commands explained.",
                        "Programming with Mosh", "1 hour", "Beginner",
                        "https://www.youtube.com/watch?v=8JJ101D3knE",
                        CourseCategory.GIT_GITHUB),

                course("GitHub Skills - Interactive Learning", "GitHub", "Git & GitHub",
                        "Official interactive GitHub learning path — free hands-on courses.",
                        "GitHub Team", "Self-paced", "Beginner",
                        "https://skills.github.com/",
                        CourseCategory.GIT_GITHUB),

                // AWS
                course("AWS Cloud Practitioner - Full Course", "YouTube - freeCodeCamp", "AWS",
                        "Complete AWS Cloud Practitioner exam prep course - covers all core services.",
                        "Andrew Brown", "13 hours", "Beginner",
                        "https://www.youtube.com/watch?v=SOTamWNgDKc",
                        CourseCategory.AWS_CLOUD),

                course("AWS for Java Developers", "YouTube - Java Brains", "AWS",
                        "Using AWS services (S3, SQS, Lambda, DynamoDB) from Java/Spring Boot.",
                        "Koushik Kothagal", "6 hours", "Intermediate",
                        "https://www.youtube.com/c/JavaBrainsChannel",
                        CourseCategory.AWS_CLOUD),

                course("AWS Free Tier - Hands On", "AWS", "AWS",
                        "Official AWS free tier with 12 months free access to core services.",
                        "AWS Team", "Self-paced", "Beginner",
                        "https://aws.amazon.com/free/",
                        CourseCategory.AWS_CLOUD),

                // DSA
                course("Data Structures & Algorithms in Java", "YouTube - Kunal Kushwaha", "DSA",
                        "Complete DSA course in Java - arrays, trees, graphs, dynamic programming.",
                        "Kunal Kushwaha", "60+ hours", "Intermediate",
                        "https://www.youtube.com/watch?v=rZ41y93P2Qo",
                        CourseCategory.DSA),

                course("DSA Sheet by Striver - Java Solutions", "take U forward", "DSA",
                        "Most popular DSA sheet for placements - 450 problems with video solutions.",
                        "Raj Vikramaditya", "Self-paced", "Intermediate",
                        "https://takeuforward.org/strivers-a2z-dsa-course/strivers-a2z-dsa-course-sheet-2/",
                        CourseCategory.DSA),

                // MICROSERVICES
                course("Microservices with Spring Boot and Spring Cloud", "YouTube - Java Brains", "Microservices",
                        "Build microservices architecture using Spring Boot, Eureka, Zuul, Feign.",
                        "Koushik Kothagal", "8 hours", "Advanced",
                        "https://www.youtube.com/playlist?list=PLqq-6Pq4lTTZSKAFG6aCDVDP86Qx4lNas",
                        CourseCategory.MICROSERVICES),

                course("Microservices Full Course", "YouTube - Telusko", "Microservices",
                        "Microservices with Docker, Kubernetes, Spring Cloud complete guide.",
                        "Navin Reddy", "10 hours", "Advanced",
                        "https://www.youtube.com/watch?v=BnknNTN8icw",
                        CourseCategory.MICROSERVICES)
        ));
        log.info("✅ Seeded {} courses", courseRepository.count());
    }

    //HELPERS

    private Job job(String title, String company, String location,
                    JobType type, CompanyType cType,
                    String desc, String req, String salary,
                    String link, String exp, String skills) {
        return Job.builder()
                .title(title).company(company).location(location)
                .jobType(type).companyType(cType)
                .description(desc).requirements(req)
                .salary(salary).applyLink(link)
                .experience(exp).skills(skills)
                .isActive(true)
                .build();
    }

    private FreeCourse course(String title, String platform, String tech,
                              String desc, String instructor, String duration,
                              String level, String link,
                              CourseCategory category) {
        return FreeCourse.builder()
                .title(title).platform(platform).technology(tech)
                .description(desc).instructor(instructor)
                .duration(duration).level(level)
                .courseLink(link).category(category)
                .build();
    }
}
