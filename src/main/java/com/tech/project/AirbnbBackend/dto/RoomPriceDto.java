package com.tech.project.AirbnbBackend.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class RoomPriceDto {

    private Long roomId;
    private String type;
    private BigDecimal totalPrice;
    private List<BigDecimal> dailyPrices;
}