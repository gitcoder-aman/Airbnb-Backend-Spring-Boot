package com.tech.project.AirbnbBackend.strategy;

import com.tech.project.AirbnbBackend.entities.Inventory;

import java.math.BigDecimal;

public interface PricingStrategy {

    BigDecimal calculatePrice(Inventory inventory);
}
