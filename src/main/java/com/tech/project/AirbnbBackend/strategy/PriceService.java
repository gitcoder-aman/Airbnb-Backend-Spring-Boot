package com.tech.project.AirbnbBackend.strategy;

import com.tech.project.AirbnbBackend.entities.Inventory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PriceService {

    public BigDecimal calculateDynamicPricing(Inventory inventory){
        PricingStrategy pricingStrategy = new BasePricingStrategy();

        //apply the additional strategy
        pricingStrategy = new SurgePriceStrategy(pricingStrategy);
        pricingStrategy = new OccupancyPricingStrategy(pricingStrategy);
        pricingStrategy = new UrgencyPricingStrategy(pricingStrategy);
        pricingStrategy = new HolidayPriceStrategy(pricingStrategy);
        return pricingStrategy.calculatePrice(inventory);
    }

    //Return the sum of price of this inventory list
    public BigDecimal calculateTotalPrice(List<Inventory>inventoryList){
        return inventoryList.stream()
                 .map(this::calculateDynamicPricing)
                 .reduce(BigDecimal.ZERO,BigDecimal::add);
    }
}
