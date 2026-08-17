package com.tech.project.AirbnbBackend;

import com.tech.project.AirbnbBackend.dto.RoomDto;
import com.tech.project.AirbnbBackend.entities.Hotel;
import com.tech.project.AirbnbBackend.entities.Room;
import com.tech.project.AirbnbBackend.entities.enums.RoomType;
import com.tech.project.AirbnbBackend.repositories.HotelRepository;
import com.tech.project.AirbnbBackend.services.RoomService;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.*;


@SpringBootTest
public class RoomSeederTest {

 /*   @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private RoomService roomService;

    private final Random random = new Random();

    private final List<String> photoPool = List.of(
            "https://images.pexels.com/photos/6876834/pexels-photo-6876834.jpeg",
            "https://images.pexels.com/photos/37734643/pexels-photo-37734643.jpeg",
            "https://images.pexels.com/photos/33389169/pexels-photo-33389169.jpeg",
            "https://images.pexels.com/photos/34496715/pexels-photo-34496715.jpeg",
            "https://images.pexels.com/photos/271624/pexels-photo-271624.jpeg",
            "https://images.pexels.com/photos/271639/pexels-photo-271639.jpeg",
            "https://images.pexels.com/photos/271618/pexels-photo-271618.jpeg",
            "https://images.pexels.com/photos/164595/pexels-photo-164595.jpeg",
            "https://images.pexels.com/photos/237371/pexels-photo-237371.jpeg",
            "https://images.pexels.com/photos/271743/pexels-photo-271743.jpeg"
    );

    private final List<String> amenitiesPool = List.of(
            "WiFi",
            "TV",
            "AC",
            "Mini Bar",
            "Coffee Machine",
            "Balcony",
            "Room Service",
            "Parking",
            "Bathtub",
            "Work Desk",
            "Hair Dryer",
            "Laundry Service",
            "Breakfast Included",
            "Swimming Pool Access",
            "Gym Access"
    );

    @Test
    void seedRoomsForAllHotels() {

        List<Hotel> hotels = hotelRepository.findAll();

        for (Hotel hotel : hotels) {
            long hotelId = hotel.getId();
            if(hotelId >= 22  && hotelId <= 71) {

                for (RoomType roomType : RoomType.values()) {

                    RoomDto request =
                            createRandomRoom(
                                    roomType
                            );

                    roomService.createNewRoomInHotel(hotel.getId(), request);

                    System.out.println(
                            "Created "
                                    + roomType
                                    + " room for hotel "
                                    + hotel.getId()
                    );
                }
            }
        }

        System.out.println(
                "\nTotal Rooms Created = "
                        + hotels.size() * RoomType.values().length
        );
    }

    private RoomDto createRandomRoom(
            RoomType roomType
    ) {

        int price =
                switch (roomType) {

                    case SINGLE ->
                            1200 + random.nextInt(800);

                    case STANDARD ->
                            2000 + random.nextInt(1500);

                    case DELUXE ->
                            3500 + random.nextInt(2000);

                    case SUPER_DELUXE ->
                            5000 + random.nextInt(3000);

                    case SUITE ->
                            8000 + random.nextInt(5000);

                    case FAMILY ->
                            6000 + random.nextInt(3000);
                };

        int capacity =
                switch (roomType) {

                    case SINGLE -> 1;

                    case STANDARD -> 2;

                    case DELUXE -> 2;

                    case SUPER_DELUXE -> 3;

                    case SUITE -> 4;

                    case FAMILY -> 5;
                };

        return RoomDto.builder()
                .type(roomType.toString())
                .basePrice(BigDecimal.valueOf(price))
                .capacity(capacity)
                .totalCount(10 + random.nextInt(25))
                .photos(getRandomPhotos().reversed().toArray(new String[0]))
                .amenities(getRandomAmenities().reversed().toArray(new String[0]))
                .description(getDescription(roomType))
                .build();
    }

    private List<String> getRandomPhotos() {

        List<String> temp = new ArrayList<>(photoPool);

        Collections.shuffle(temp);

        return temp.subList(0, 4);
    }

    private List<String> getRandomAmenities() {

        List<String> temp = new ArrayList<>(amenitiesPool);

        Collections.shuffle(temp);

        return temp.subList(0, 5);
    }

    private String getDescription(RoomType roomType) {

        return switch (roomType) {

            case SINGLE ->
                    "Comfortable single room with modern amenities.";

            case STANDARD ->
                    "Standard room suitable for couples and business travellers.";

            case DELUXE ->
                    "Spacious deluxe room with premium furnishings.";

            case SUPER_DELUXE ->
                    "Luxury super deluxe room with city view.";

            case SUITE ->
                    "Executive suite with separate living area.";

            case FAMILY ->
                    "Large family room designed for group stays.";
        };
    }
    */
}
