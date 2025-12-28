package com.tech.project.AirbnbBackend.services.impl;

import com.tech.project.AirbnbBackend.dto.HotelDto;
import com.tech.project.AirbnbBackend.entities.Hotel;
import com.tech.project.AirbnbBackend.entities.Room;
import com.tech.project.AirbnbBackend.exception.ResourceNotFoundException;
import com.tech.project.AirbnbBackend.repositories.HotelRepository;
import com.tech.project.AirbnbBackend.services.HotelService;
import com.tech.project.AirbnbBackend.services.InventoryService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class HotelServiceImpl implements HotelService {

    private final HotelRepository hotelRepository;
    private final ModelMapper modelMapper;
    private final InventoryService inventoryService;

    @Override
    public HotelDto createNewHotel(HotelDto hotelDto) {

        log.info("Creating a new Hotel with name: {}",hotelDto.getName());

        Hotel hotel = modelMapper.map(hotelDto,Hotel.class);
        hotel.setActive(false);
        hotel = hotelRepository.save(hotel);
        log.info("Created a new Hotel with Id: {}",hotel.getId());
        return modelMapper.map(hotel,HotelDto.class);
    }

    @Override
    public HotelDto getHotelById(Long id) {
        log.info("Getting the hotel with ID: {} ",id);
       Hotel hotel =  hotelRepository
               .findById(id)
               .orElseThrow(()->new ResourceNotFoundException("Hotel not found with ID"+id));
       return modelMapper.map(hotel,HotelDto.class);
    }

    @Override
    public HotelDto updateHotelById(Long id, HotelDto hotelDto) {
        Hotel hotel =  hotelRepository
                .findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Hotel not found with ID"+id));
        modelMapper.map(hotelDto,hotel);
        hotelDto.setId(hotel.getId());
        hotel = hotelRepository.save(hotel);
        return modelMapper.map(hotel,HotelDto.class);
    }

    @Transactional
    @Override
    public void deleteHotelById(Long id) {
        Hotel hotel =  hotelRepository
                .findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Hotel not found with ID"+id));

        hotelRepository.deleteById(id);
        // delete the future inventory for this hotel
        for (Room room: hotel.getRooms()){
            inventoryService.deleteFutureInventory(room);
        }
    }

    @Override
    public HotelDto updateParticularFieldById(Long hotelId, HotelDto hotelDto) {
        Hotel hotel =  hotelRepository
                .findById(hotelId)
                .orElseThrow(()->new ResourceNotFoundException("Hotel not found with ID"+hotelId));
//        updates.forEach((field,value)->{
//            Field fieldToBeUpdated = ReflectionUtils.findField(Hotel.class,field);
//            assert fieldToBeUpdated != null;
//            fieldToBeUpdated.setAccessible(true);
//            ReflectionUtils.setField(fieldToBeUpdated,hotel,value);
//        });  //TODO: This Reflection use is not best practices

        if (hotelDto.getName() != null) {
            hotel.setName(hotelDto.getName());
        }

        if (hotelDto.getCity() != null) {
            hotel.setCity(hotelDto.getCity());
        }

        if (hotelDto.getContactInfo().getAddress() != null) {
            hotel.getContactInfo().setAddress(hotelDto.getContactInfo().getAddress());
        }
        if(hotelDto.getActive() != null){
            hotel.setActive(hotelDto.getActive());
        }
        if(hotelDto.getPhotos().length > 0){
            hotel.setPhotos(hotelDto.getPhotos());
        }
        if(hotelDto.getAmenities().length > 0){
            hotel.setAmenities(hotelDto.getAmenities());
        }
        if(hotelDto.getContactInfo().getEmail() != null){
            hotel.getContactInfo().setAddress(hotelDto.getContactInfo().getEmail());
        }
        if(hotelDto.getContactInfo().getLocation() != null){
            hotel.getContactInfo().setLocation(hotelDto.getContactInfo().getLocation());
        }
        if(hotelDto.getContactInfo().getPhoneNumber() != null){
            hotel.getContactInfo().setPhoneNumber(hotelDto.getContactInfo().getPhoneNumber());
        }

        var updatedHotelField = hotelRepository.save(hotel);
        return modelMapper.map(updatedHotelField,HotelDto.class);
    }

    @Override
    @Transactional
    public HotelDto activateHotel(Long hotelId) {
        log.info("Activating the hotel with ID: {}",hotelId);
        Hotel hotel =  hotelRepository
                .findById(hotelId)
                .orElseThrow(()->new ResourceNotFoundException("Hotel not found with ID"+hotelId));
        hotel.setActive(true);
        //Create inventory for all the rooms for this hotel
        for (Room room : hotel.getRooms()){
            inventoryService.initializeRoomForAYear(room);
        }
        return modelMapper.map(hotel,HotelDto.class);
    }
}
