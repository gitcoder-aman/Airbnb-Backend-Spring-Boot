package com.tech.project.AirbnbBackend.controllers.ai;

import com.tech.project.AirbnbBackend.advice.ApiResponse;
import com.tech.project.AirbnbBackend.controllers.user.HotelBrowseController;
import com.tech.project.AirbnbBackend.dto.HotelDetailsRequest;
import com.tech.project.AirbnbBackend.dto.HotelPriceDto;
import com.tech.project.AirbnbBackend.dto.HotelSearchRequest;
import com.tech.project.AirbnbBackend.dto.SearchCriteria;
import com.tech.project.AirbnbBackend.entities.Hotel;
import com.tech.project.AirbnbBackend.entities.HotelContactInfo;
import com.tech.project.AirbnbBackend.services.AiService;
import com.tech.project.AirbnbBackend.services.HotelService;
import com.tech.project.AirbnbBackend.services.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.ObjectMapper;

import java.sql.SQLOutput;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
@Slf4j
public class AiController {

    private final AiService aiService;
    private final ObjectMapper objectMapper;  // Jackson convert JSON to dto
    private final InventoryService inventoryService;
    private final HotelService hotelService;


    @PostMapping("/chat")
    ResponseEntity<ApiResponse<String>> ask(@RequestBody String question) {
        return ResponseEntity.ok(new ApiResponse<>(aiService.chat(question)));
    }

    @PostMapping("/search")
    public ResponseEntity<ApiResponse<String>> search(@RequestBody String prompt) throws Exception {

        String json = aiService.extractCriteria(prompt);

        SearchCriteria criteria =
                objectMapper.readValue(
                        json,
                        SearchCriteria.class
                );

        String city = criteria.getCity();
        LocalDate checkInDate = criteria.getCheckIn();
        LocalDate checkOutDate = criteria.getCheckOut();
        Double maxPrice = criteria.getMaxPrice();

        if (city == null || city.isBlank()) {
            throw new BadRequestException(
                    "Please specify a city. Example: Hotels in Bangalore under 5000"
            );
        }

        if (checkInDate == null) {
            throw new BadRequestException(
                    "Check-in date is required"
            );
        }

        if (checkOutDate == null) {
            throw new BadRequestException(
                    "Check-out date is required"
            );
        }

        HotelSearchRequest hotelSearchRequest = new HotelSearchRequest();
        hotelSearchRequest.setCity(city);
        hotelSearchRequest.setCheckInDate(checkInDate);
        hotelSearchRequest.setCheckOutDate(checkOutDate);
        hotelSearchRequest.setMaxPrice(maxPrice);

        Page<HotelPriceDto> hotelPriceDtos = inventoryService.searchHotels(hotelSearchRequest);
        log.info("@size{}", hotelPriceDtos.getContent().size());
        StringBuilder sb = new StringBuilder();

        sb.append("Available hotels:\n\n");

        hotelPriceDtos.getContent().forEach(h -> {
            sb.append("• ")
                    .append(h.getHotel().getName())
                    .append(" - ₹")
                    .append(Math.round(h.getPrice()))
                    .append("\n");
        });

        return ResponseEntity.ok(new ApiResponse<>(sb.toString()));
    }

    @PostMapping("/hotel-detail")
    public ResponseEntity<ApiResponse<String>> hotelDetail(@RequestBody String prompt) throws Exception {

        HotelDetailsRequest json = aiService.extractHotelName(prompt);

        log.info("@@hotel json:{}", json);

        String hotelName = json.getHotelName();

        if (hotelName == null || hotelName.isBlank()) {
            throw new BadRequestException(
                    "Please specify a hotel name. Example: Tell me about Taj Bangalore"
            );
        }
        List<Hotel> hotels = hotelService.getHotelByHotelName(hotelName);

        String hotelData = hotels.stream()
                .map(hotel -> """
                Hotel Name: %s
                City: %s
                Amenities: %s
                Starting Price: %s
                Contact Info: %s
                """
                        .formatted(
                                hotel.getName(),
                                hotel.getCity(),
                                String.join(", ", hotel.getAmenities()),
                                hotel.getStartingPrice(),
                                hotel.getContactInfo()
                        ))
                .collect(Collectors.joining("\n\n"));

        String res  = aiService.toNaturalLanguage(hotelData,hotelName);

//        StringBuilder response = new StringBuilder();

//        for(Hotel hotel : hotels) {
//
//            String amenities = String.join(", ", hotel.getAmenities());
//            HotelContactInfo contactInfo = hotel.getContactInfo();
//            String contact = "Address="+contactInfo.getAddress()+", Email="+contactInfo.getEmail()+", Phone Number="+contactInfo.getPhoneNumber()+", Location="+contactInfo.getLocation();
//            response.append("""
//                    Hotel Name: %s
//                    City: %s
//                    Amenities: %s
//                    Starting Price: %s
//                    Contact Info: %s
//                    """
//                    .formatted(
//                            hotel.getName(),
//                            hotel.getCity(),
//                            amenities,
//                            hotel.getStartingPrice(),
//                            contact
//                    ));
//        }
        System.out.println(res);

        return ResponseEntity.ok(new ApiResponse<>(res));
    }
}