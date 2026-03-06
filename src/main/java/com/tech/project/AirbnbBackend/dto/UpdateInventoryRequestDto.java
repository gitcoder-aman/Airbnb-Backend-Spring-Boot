package com.tech.project.AirbnbBackend.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class UpdateInventoryRequestDto {
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private BigDecimal surgeFactor;
    private Boolean closed;
}
