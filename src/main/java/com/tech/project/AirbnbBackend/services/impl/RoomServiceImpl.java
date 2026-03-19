package com.tech.project.AirbnbBackend.services.impl;

import com.tech.project.AirbnbBackend.dto.RoomDto;
import com.tech.project.AirbnbBackend.entities.Hotel;
import com.tech.project.AirbnbBackend.entities.Inventory;
import com.tech.project.AirbnbBackend.entities.Room;
import com.tech.project.AirbnbBackend.entities.User;
import com.tech.project.AirbnbBackend.exception.ResourceNotFoundException;
import com.tech.project.AirbnbBackend.exception.UnAuthorisedException;
import com.tech.project.AirbnbBackend.repositories.HotelRepository;
import com.tech.project.AirbnbBackend.repositories.InventoryRepository;
import com.tech.project.AirbnbBackend.repositories.RoomRepository;
import com.tech.project.AirbnbBackend.services.InventoryService;
import com.tech.project.AirbnbBackend.services.RoomService;
import com.tech.project.AirbnbBackend.strategy.PriceService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.tech.project.AirbnbBackend.utils.AppUtils.getCurrentUser;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;
    private final ModelMapper modelMapper;
    private final InventoryService inventoryService;
    private final InventoryRepository inventoryRepository;
    private final PriceService priceService;

    @Transactional
    @Override
    public RoomDto createNewRoomInHotel(Long hotelId, RoomDto roomDto) {
        log.info("Creating a new Room in hotel with ID: {}", hotelId);
        Hotel hotel = hotelRepository
                .findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with ID" + hotelId));

        //just we have to assure the admin can create own hotel room  not other
        User user = getCurrentUser();
        assert user != null;
        if(!user.getId().equals(hotel.getOwner().getId())){
            throw new UnAuthorisedException("This user does not own this hotel with id: "+hotelId);
        }

        Room room = modelMapper.map(roomDto, Room.class);
        room.setHotel(hotel);
        room = roomRepository.save(room);

        //create inventory when hotel is active
        if(hotel.getActive()){
            inventoryService.initializeRoomForAYear(room);
        }

        //update the hotel starting price
        BigDecimal startingPrice =  roomRepository.findMinPriceByHotelId(hotelId);
        hotel.setStartingPrice(startingPrice);
        hotelRepository.save(hotel);

        return modelMapper.map(room, RoomDto.class);
    }

    @Override
    public List<RoomDto> getAllRoomInHotelById(Long hotelId) {

        log.info("Getting all rooms in hotel with ID: {}", hotelId);
        Hotel hotel = hotelRepository
                .findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with ID" + hotelId));

        //just we have to assure the admin can get all room own hotel not other
        User user = getCurrentUser();
        assert user != null;
        if(!user.equals(hotel.getOwner())){
            throw new UnAuthorisedException("This user does not own this hotel with id: "+hotelId);
        }
        return hotel.getRooms()
                .stream()
                .map((element) -> modelMapper.map(element, RoomDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public RoomDto getRoomById(Long roomId) {
        log.info("Getting the room with ID: {}", roomId);
        Room room = roomRepository
                .findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with ID" + roomId));

        return modelMapper.map(room, RoomDto.class);
    }

    @Transactional
    @Override
    public void deleteRoomById(Long roomId) {
        log.info("Deleting the room with ID: {}", roomId);
        Room room = roomRepository
                .findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with ID" + roomId));


        //just we have to assure the admin can delete room own hotel not other
        User user = getCurrentUser();
        assert user != null;
        if(!user.equals(room.getHotel().getOwner())){
            throw new UnAuthorisedException("This user does not own this room with id: "+roomId);
        }
       // delete all future inventory for this room
         // because if we delete first room then violate foreign key throw exception
        inventoryService.deleteAllInventories(room);  // first delete inventory data of room then delete room
       roomRepository.deleteById(roomId);

    }

    @Override
    @Transactional
    public RoomDto updateRoomById(Long hotelId,Long roomId, RoomDto roomDto) {
        log.info("Updating the room with ID: {}", roomId);

        Hotel hotel = hotelRepository
                .findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with ID" + hotelId));

        Room room = roomRepository
                .findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with ID" + roomId));

        //just we have to assure the admin can get all room own hotel not other
        User user = getCurrentUser();
        assert user != null;
        if(!user.equals(hotel.getOwner())){
            throw new UnAuthorisedException("This user does not own this hotel with id: "+hotelId);
        }
        modelMapper.map(roomDto,room);
        roomDto.setId(roomId);

        //if price or inventory is updated then update the inventory for this room
        BigDecimal price = room.getBasePrice();
        List<Inventory> inventoryList = inventoryRepository.findInventoryByRoomId(roomId);

        inventoryList.forEach(inventory -> {

            // Step 1: update base price
            inventory.setPrice(price);

            // Step 2: calculate dynamic price
            BigDecimal dynamicPrice = priceService.calculateDynamicPricing(inventory);

            // Step 3: set final price
            inventory.setPrice(dynamicPrice);
        });
        inventoryRepository.saveAll(inventoryList);
        room = roomRepository.save(room);
        return modelMapper.map(room, RoomDto.class);
    }
}
