package com.kunj.JobApp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class JobAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(JobAppApplication.class, args);
	}

}
