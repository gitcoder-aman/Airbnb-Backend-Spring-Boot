package com.tech.project.AirbnbBackend.services.impl;

import com.tech.project.AirbnbBackend.services.AiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiServiceImpl implements AiService {

    private final ChatClient chatClient;

    @Override
    public String chat(String prompt) {

        log.info("@aiAsk{}",prompt);
        String value = Objects.requireNonNull(chatClient.prompt()
                        .user(prompt)
                        .call()
                        .content())
                .trim();

        log.info("@aiAsk value{}",value);
        return value;
    }

    @Override
    public String extractCriteria(String prompt) {
        return "";
    }
}
