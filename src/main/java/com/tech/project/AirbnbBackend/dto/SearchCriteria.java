package com.tech.project.AirbnbBackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchCriteria {

    private String city;

    private Double maxPrice;

    private LocalDate checkIn;

    private LocalDate checkOut;
}