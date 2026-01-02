package com.tech.project.AirbnbBackend.services;

import com.tech.project.AirbnbBackend.entities.Hotel;
import com.tech.project.AirbnbBackend.entities.HotelMinPrice;
import com.tech.project.AirbnbBackend.entities.Inventory;
import com.tech.project.AirbnbBackend.repositories.HotelMinPriceRepository;
import com.tech.project.AirbnbBackend.repositories.HotelRepository;
import com.tech.project.AirbnbBackend.repositories.InventoryRepository;
import com.tech.project.AirbnbBackend.strategy.PriceService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PricingUpdateService {

    private final HotelRepository hotelRepository;
    private final InventoryRepository inventoryRepository;
    private final HotelMinPriceRepository hotelMinPriceRepository;
    private final PriceService priceService;

    @Scheduled(cron = "0 */2 * * * *") //second,minute hour,day,month,week
    public void updatePrice() {
        int page = 0;
        int batchSize = 100;

        while (true) {
            Page<Hotel> hotePage = hotelRepository.findAll(PageRequest.of(page, batchSize));
            if (hotePage.isEmpty()) {
                break;
            }
            hotePage.getContent().forEach(hotel -> updateHotelPrices(hotel));
            page++;
        }
    }

    private void updateHotelPrices(Hotel hotel) {

        log.info("Updating hotel prices for hotel Id:{} ",hotel.getId());
        LocalDate checkInDate = LocalDate.now();
        LocalDate checkOutDate = LocalDate.now().plusYears(1);

        List<Inventory> inventoryList = inventoryRepository.findByHotelAndDateBetween(hotel, checkInDate, checkOutDate);
        updateInventoryPrices(inventoryList);

        updateHotelMinPrice(hotel, inventoryList, checkInDate, checkOutDate);
    }

    private void updateHotelMinPrice(Hotel hotel, List<Inventory> inventoryList, LocalDate checkInDate, LocalDate checkOutDate) {
        //It finds the minimum room price for each date from the inventory list and stores it in a map.
        Map<LocalDate, BigDecimal> dailyMinPrices = inventoryList.stream()
                .collect(Collectors.groupingBy(
                        (Inventory inventory) -> inventory.getDate(),
                        Collectors.mapping((Inventory inventory) -> inventory.getPrice(), Collectors.minBy(Comparator.naturalOrder()))
                )).entrySet().stream()
                .collect(Collectors.toMap(entry->entry.getKey(), entry -> entry.getValue().orElse(BigDecimal.ZERO)));

        //Prepare HotelPrice entities in bulk
        List<HotelMinPrice>hotelMinPrices = new ArrayList<>();
        dailyMinPrices.forEach((date,price)->{
            HotelMinPrice hotelMinPrice = hotelMinPriceRepository.findByHotelAndDate(hotel,date).orElse(new HotelMinPrice(hotel,date));
            hotelMinPrice.setPrice(price);
            hotelMinPrices.add(hotelMinPrice);
        });
        //save all HotelPrice entities in bulk
        hotelMinPriceRepository.saveAll(hotelMinPrices);
    }

    private void updateInventoryPrices(List<Inventory> inventoryList) {
        inventoryList.forEach(inventory -> {
            BigDecimal dynamicPrice = priceService.calculateDynamicPricing(inventory);
            inventory.setPrice(dynamicPrice);
        });
        inventoryRepository.saveAll(inventoryList);
    }


}
