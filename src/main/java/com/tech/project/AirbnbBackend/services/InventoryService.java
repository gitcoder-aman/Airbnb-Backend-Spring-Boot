package com.tech.project.AirbnbBackend.services;

import com.tech.project.AirbnbBackend.dto.HotelPriceDto;
import com.tech.project.AirbnbBackend.dto.HotelSearchRequest;
import com.tech.project.AirbnbBackend.dto.InventoryDto;
import com.tech.project.AirbnbBackend.dto.UpdateInventoryRequestDto;
import com.tech.project.AirbnbBackend.entities.Room;
import org.springframework.data.domain.Page;

import java.util.List;

public interface InventoryService {

    void initializeRoomForAYear(Room room);

    void deleteAllInventories(Room room);

    Page<HotelPriceDto> searchHotels(HotelSearchRequest hotelSearchRequest);

    List<InventoryDto> getAllInventoriesByRoom(Long roomId);

    void updateInventory(Long roomId, UpdateInventoryRequestDto updateInventoryRequestDto);
}
