package com.tech.project.AirbnbBackend.services;

import com.tech.project.AirbnbBackend.dto.HotelDetailsRequest;

public interface AiService {
     String chat(String prompt);

     String extractCriteria(String prompt);

     HotelDetailsRequest extractHotelName(String prompt);


     String toNaturalLanguage(String hotelData,String hotelName);
}
