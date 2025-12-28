package com.tech.project.AirbnbBackend.dto;

import com.tech.project.AirbnbBackend.entities.HotelContactInfo;
import lombok.Data;

@Data
public class HotelDto {

    private Long id;

    private String name;

    private String city;

    private String[] photos;

    private String[] amenities;

    private HotelContactInfo contactInfo;

    private Boolean active;

}
