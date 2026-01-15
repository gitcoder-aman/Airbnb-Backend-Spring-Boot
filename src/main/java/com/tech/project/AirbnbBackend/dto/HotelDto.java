package com.tech.project.AirbnbBackend.dto;

import com.tech.project.AirbnbBackend.entities.HotelContactInfo;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class HotelDto {

    private Long id;

    @NotBlank(message = "Hotel name can't be empty")
    private String name;

    @NotBlank(message = "Hotel city can't be empty")
    private String city;

    @NotNull(message = "Photos cannot be null")
    @Size(min = 1, message = "At least one hotel photo is required")
    private String[] photos;

    @NotNull(message = "Amenities cannot be null")
    @Size(min = 1, message = "At least one amenity is required")
    private String[] amenities;

    @NotNull(message = "Hotel contact information is required")
    @Valid   //  IMPORTANT (nested validation)
    private HotelContactInfo contactInfo;

    private Boolean active;
}
