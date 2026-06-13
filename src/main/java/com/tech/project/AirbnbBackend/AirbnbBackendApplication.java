package com.tech.project.AirbnbBackend;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AirbnbBackendApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(AirbnbBackendApplication.class, args);
	}

	@Value("${spring.ai.ollama.chat.options.model:NOT_FOUND}")
	private String model;


	@Override
	public void run(String... args) {
		System.out.println("MODEL = " + model);
	}
}
