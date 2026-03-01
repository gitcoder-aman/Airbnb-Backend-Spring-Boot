package com.tech.project.AirbnbBackend.strategy;

import com.tech.project.AirbnbBackend.entities.Inventory;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@RequiredArgsConstructor
public class HolidayPriceStrategy implements PricingStrategy{

    private final PricingStrategy wrapped;

    //https://calendarific.com/api/v2/holidays?&api_key=Kvj0Y1iMP8iMnSVQQaTeCT6ohsz9Ixof&country=in&year=2026
    @Override
    public BigDecimal calculatePrice(Inventory inventory) {
        BigDecimal price = wrapped.calculatePrice(inventory);
        boolean isTodayHoliday = true; // call an API or check with local data
        if(isTodayHoliday){
            price = price.multiply(BigDecimal.valueOf(1.15));
        }
        return price;
    }
}
