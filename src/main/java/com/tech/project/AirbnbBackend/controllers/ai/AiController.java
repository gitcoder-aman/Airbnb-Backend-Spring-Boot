package com.tech.project.AirbnbBackend.controllers.ai;

import com.tech.project.AirbnbBackend.advice.ApiResponse;
import com.tech.project.AirbnbBackend.services.AiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
@Slf4j
public class AiController {

    private final AiService aiService;

    @PostMapping("/chat")
    ResponseEntity<ApiResponse<String>> ask(@RequestBody String question){
        return ResponseEntity.ok(new ApiResponse<>(aiService.chat(question)));
    }
}