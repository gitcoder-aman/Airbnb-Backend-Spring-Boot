package com.tech.project.AirbnbBackend.strategy;

import com.tech.project.AirbnbBackend.entities.Inventory;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;


//Surge pricing is a dynamic pricing strategy where hotel room rates rise when demand increases.

@RequiredArgsConstructor
public class SurgePriceStrategy implements PricingStrategy{

    private final PricingStrategy wrapped;

    @Override
    public BigDecimal calculatePrice(Inventory inventory) {
        BigDecimal price = wrapped.calculatePrice(inventory);
        return price.multiply(inventory.getSurgeFactor());
    }
}
