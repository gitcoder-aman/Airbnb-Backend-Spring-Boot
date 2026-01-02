package com.tech.project.AirbnbBackend.strategy;

import com.tech.project.AirbnbBackend.entities.Inventory;

import java.math.BigDecimal;

public class BasePricingStrategy implements PricingStrategy{
    @Override
    public BigDecimal calculatePrice(Inventory inventory) {
        return inventory.getRoom().getBasePrice();
    }
}
