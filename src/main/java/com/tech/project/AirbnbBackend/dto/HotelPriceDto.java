package com.tech.project.AirbnbBackend.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tech.project.AirbnbBackend.entities.Hotel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HotelPriceDto {
    private Hotel hotel;
    private Double price;
}
