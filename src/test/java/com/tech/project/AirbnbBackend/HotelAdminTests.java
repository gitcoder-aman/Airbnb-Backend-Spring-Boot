package com.tech.project.AirbnbBackend;

import com.tech.project.AirbnbBackend.dto.HotelDto;
import com.tech.project.AirbnbBackend.entities.HotelContactInfo;
import com.tech.project.AirbnbBackend.services.HotelService;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Slf4j
public class HotelAdminTests {

    @Autowired
    private HotelService hotelService;

    @Test
    void createNewHotel() {

        HotelDto hotelDto = new HotelDto();
        hotelDto.setName("Hotel 3");
        hotelDto.setActive(false);
        hotelDto.setCity("Bangalore");
        hotelDto.setPhotos(new String[]{"photo1.jpg", "photo2.jpg", "photo3.jpg"});
        hotelDto.setAmenities(new String[]{"amenities1.jpg", "amenities2.jpg"});
        HotelContactInfo hotelContactInfo = new HotelContactInfo();
        hotelContactInfo.setEmail("amankumar@gmail.com");
        hotelContactInfo.setAddress("Bangalore");
        hotelContactInfo.setLocation("121.32,145.34");
        hotelContactInfo.setPhoneNumber("5234543534");
        hotelDto.setContactInfo(hotelContactInfo);

        assertThrows(ConstraintViolationException.class, () -> {
            HotelDto newHotel = hotelService.createNewHotel(hotelDto);
            log.info("Created a new Hotel : {}", newHotel);
        });
    }
}
