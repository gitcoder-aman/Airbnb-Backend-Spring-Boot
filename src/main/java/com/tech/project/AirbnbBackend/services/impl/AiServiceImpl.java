package com.tech.project.AirbnbBackend.services.impl;

import com.tech.project.AirbnbBackend.dto.HotelDetailsRequest;
import com.tech.project.AirbnbBackend.dto.HotelSearchRequest;
import com.tech.project.AirbnbBackend.entities.Hotel;
import com.tech.project.AirbnbBackend.services.AiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiServiceImpl implements AiService {

    private final ChatClient chatClient;

    @Override
    public String chat(String prompt) {

        log.info("@aiAsk{}", prompt);
        String value = Objects.requireNonNull(chatClient.prompt()
                        .user(prompt)
                        .call()
                        .content())
                .trim();

        log.info("@aiAsk value{}", value);
        return value;
    }

    @Override
    public String extractCriteria(String prompt) {   // they are returning json format

        String systemPrompt = """
                You are a hotel search criteria extraction engine.
                
                Today's date is %s.
                
                Extract hotel search criteria from the user's query.
                
                Rules:
                1. Return ONLY valid JSON.
                2. Do not return explanations.
                3. Do not return markdown or code fences.
                4. Never invent city names.
                5. Never invent prices.
                6. If a field is not mentioned, return null.
                7. If the user specifies day and month but not year, use the current year.
                8. Convert relative dates (today, tomorrow, next week, next weekend) into actual dates using today's date.
                9. Dates must be in yyyy-MM-dd format.
                10. maxPrice must be a number.
                11. If the query is not about hotel search, return all fields as null.
                
                Return JSON in exactly this format:
                
                {
                  "city": null,
                  "checkIn": null,
                  "checkOut": null,
                  "maxPrice": null
                }
                
                Examples:
                
                User:
                hotels in Bangalore
                
                Output:
                {
                  "city":"Bangalore",
                  "checkIn":null,
                  "checkOut":null,
                  "maxPrice":null
                }
                
                User:
                hotels in Bangalore under 5000
                
                Output:
                {
                  "city":"Bangalore",
                  "checkIn":null,
                  "checkOut":null,
                  "maxPrice":5000
                }
                
                User:
                hotels in Bangalore from 17 June to 20 June
                
                Output:
                {
                  "city":"Bangalore",
                  "checkIn":"2026-06-17",
                  "checkOut":"2026-06-20",
                  "maxPrice":null
                }
                
                User:
                hotels in Mumbai from 10 July to 12 July under 8000
                
                Output:
                {
                  "city":"Mumbai",
                  "checkIn":"2026-07-10",
                  "checkOut":"2026-07-12",
                  "maxPrice":8000
                }
                """.formatted(LocalDate.now());

        return chatClient
                .prompt()
                .system(systemPrompt)
                .user(prompt)
                .call()
                .content();
    }

    @Override
    public HotelDetailsRequest extractHotelName(String prompt) {

        String systemPrompt = """
                Extract hotel name.
                
                IMPORTANT:
                Return ONLY valid JSON.
                Do not explain.
                Do not add markdown.
                Do not add text before or after JSON.
                Do not text before JSON.
                Do not text after JSON.
                
                Example:
                
                {
                  "hotelName":"Taj Bangalore"
                }
                """;
        return chatClient
                .prompt()
                .system(systemPrompt)
                .user(prompt)
                .call()
                .entity(HotelDetailsRequest.class);
    }
}
