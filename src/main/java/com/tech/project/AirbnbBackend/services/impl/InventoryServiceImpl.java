package com.tech.project.AirbnbBackend.services.impl;

import com.tech.project.AirbnbBackend.dto.HotelPriceDto;
import com.tech.project.AirbnbBackend.dto.HotelSearchRequest;
import com.tech.project.AirbnbBackend.dto.InventoryDto;
import com.tech.project.AirbnbBackend.dto.UpdateInventoryRequestDto;
import com.tech.project.AirbnbBackend.entities.Inventory;
import com.tech.project.AirbnbBackend.entities.Room;
import com.tech.project.AirbnbBackend.entities.User;
import com.tech.project.AirbnbBackend.exception.ResourceNotFoundException;
import com.tech.project.AirbnbBackend.repositories.HotelMinPriceRepository;
import com.tech.project.AirbnbBackend.repositories.InventoryRepository;
import com.tech.project.AirbnbBackend.repositories.RoomRepository;
import com.tech.project.AirbnbBackend.services.InventoryService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import static com.tech.project.AirbnbBackend.utils.AppUtils.getCurrentUser;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final HotelMinPriceRepository hotelMinPriceRepository;
    private final RoomRepository roomRepository;
    private final ModelMapper modelMapper;

    @Override
    public void initializeRoomForAYear(Room room) {
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusYears(1);
        for (;!today.isAfter(endDate);today = today.plusDays(1)){
            Inventory inventory = Inventory.builder()
                    .hotel(room.getHotel())
                    .room(room)
                    .bookedCount(0)
                    .reservedCount(0)
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

    @Override
    public List<InventoryDto> getAllInventoriesByRoom(Long roomId) {

        log.info("Getting All inventory by room for room with id:{}",roomId);
        Room room = roomRepository.findById(roomId)
                .orElseThrow(()-> new ResourceNotFoundException("Room not found with id: "+roomId));
        User user = getCurrentUser();
        if(!user.equals(room.getHotel().getOwner())){
            throw new AccessDeniedException("You are not the owner oh this id: "+roomId);
        }
        return inventoryRepository.findByRoomOrderByDate(room)
                .stream()
                .map((element) -> modelMapper.map(element, InventoryDto.class))
                .collect(Collectors.toList());

    }

    @Override
    @Transactional
    public void updateInventory(Long roomId, UpdateInventoryRequestDto updateInventoryRequestDto) {
        log.info("Updating All inventory by room for room with id:{} between date range:{} - {}",roomId,updateInventoryRequestDto.getCheckInDate(),updateInventoryRequestDto.getCheckOutDate());
        Room room = roomRepository.findById(roomId)
                .orElseThrow(()-> new ResourceNotFoundException("Room not found with id: "+roomId));
        User user = getCurrentUser();
        if(!user.equals(room.getHotel().getOwner())){
            throw new AccessDeniedException("You are not the owner oh this id: "+roomId);
        }
        inventoryRepository.getInventoryAndLockBeforeUpdate(
                roomId,
                updateInventoryRequestDto.getCheckInDate(),
                updateInventoryRequestDto.getCheckOutDate()
        );
        inventoryRepository.updateInventory(
                roomId,
                updateInventoryRequestDto.getCheckInDate(),
                updateInventoryRequestDto.getCheckOutDate(),
                updateInventoryRequestDto.getSurgeFactor(),
                updateInventoryRequestDto.getClosed()
        );
    }
}
