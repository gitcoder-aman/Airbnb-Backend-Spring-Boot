package com.tech.project.AirbnbBackend.services.impl;

import com.tech.project.AirbnbBackend.dto.HotelPriceDto;
import com.tech.project.AirbnbBackend.dto.HotelSearchRequest;
import com.tech.project.AirbnbBackend.entities.Inventory;
import com.tech.project.AirbnbBackend.entities.Room;
import com.tech.project.AirbnbBackend.exception.InvalidDateException;
import com.tech.project.AirbnbBackend.repositories.HotelMinPriceRepository;
import com.tech.project.AirbnbBackend.repositories.InventoryRepository;
import com.tech.project.AirbnbBackend.services.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final HotelMinPriceRepository hotelMinPriceRepository;

    @Override
    public void initializeRoomForAYear(Room room) {
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(4);
        for (;!today.isAfter(endDate);today = today.plusDays(1)){
            Inventory inventory = Inventory.builder()
                    .hotel(room.getHotel())
                    .room(room)
                    .bookCount(0)
                    .reversedCount(0)
                    .city(room.getHotel().getCity())
                    .date(today)
                    .price(room.getBasePrice())
                    .surgeFactor(BigDecimal.ONE)
                    .totalCount(room.getTotalCount())
                    .closed(false)
                    .build();
            inventoryRepository.save(inventory);
        }
    }

    @Override
    public void deleteAllInventories(Room room) {
        inventoryRepository.deleteByRoom(room);

    }

    @Override
    public Page<HotelPriceDto> searchHotels(HotelSearchRequest hotelSearchRequest) {
        Pageable pageable = PageRequest.of(hotelSearchRequest.getPage(),hotelSearchRequest.getSize());
        long dateCount = ChronoUnit.DAYS.between(hotelSearchRequest.getCheckInDate(),hotelSearchRequest.getCheckOutDate()) + 1;


        Page<HotelPriceDto> hotelsWithAvailableInventory = hotelMinPriceRepository.findHotelsWithAvailableInventory(
                                                                            hotelSearchRequest.getCity(),
                                                                            hotelSearchRequest.getCheckInDate(),
                                                                            hotelSearchRequest.getCheckOutDate(),
                                                                            hotelSearchRequest.getNumberOfRooms(),
                                                                            dateCount,
                                                                            pageable);

        return hotelsWithAvailableInventory;
    }
}
