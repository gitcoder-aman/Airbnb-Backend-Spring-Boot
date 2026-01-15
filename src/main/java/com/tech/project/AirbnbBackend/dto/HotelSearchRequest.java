package com.tech.project.AirbnbBackend.dto;

import lombok.Data;

import java.time.LocalDate;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class HotelSearchRequest {

    @NotBlank(message = "City is required")
    private String city;

    @NotNull(message = "Check-in date is required")
    @FutureOrPresent(message = "Check-in date cannot be in the past")
    private LocalDate checkInDate;

    @NotNull(message = "Check-out date is required")
    @Future(message = "Check-out date must be in the future")
    private LocalDate checkOutDate;

    @NotNull(message = "Number of rooms is required")
    @Min(value = 1, message = "At least one room must be requested")
    private Integer numberOfRooms;

    @Min(value = 0, message = "Page index cannot be negative")
    private Integer page = 0;

    @Min(value = 1, message = "Page size must be at least 1")
    @Max(value = 100, message = "Page size cannot exceed 100")
    private Integer size = 10;

    //  Cross-field validation
    @AssertTrue(message = "Check-out date must be after check-in date")
    public boolean isValidDateRange() {
        if (checkInDate == null || checkOutDate == null) {
            return true;
        }
        return checkOutDate.isAfter(checkInDate);
    }
}
