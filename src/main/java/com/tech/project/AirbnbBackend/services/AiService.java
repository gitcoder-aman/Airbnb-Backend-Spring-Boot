package com.tech.project.AirbnbBackend.services;

public interface AiService {
     String chat(String prompt);

     String extractCriteria(String prompt);
}
