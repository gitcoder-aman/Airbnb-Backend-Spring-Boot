package com.tech.project.AirbnbBackend.services.impl;

import com.tech.project.AirbnbBackend.dto.HotelDto;
import com.tech.project.AirbnbBackend.dto.HotelInfoDto;
import com.tech.project.AirbnbBackend.dto.RoomDto;
import com.tech.project.AirbnbBackend.entities.Hotel;
import com.tech.project.AirbnbBackend.entities.Room;
import com.tech.project.AirbnbBackend.entities.User;
import com.tech.project.AirbnbBackend.exception.ResourceNotFoundException;
import com.tech.project.AirbnbBackend.exception.UnAuthorisedException;
import com.tech.project.AirbnbBackend.repositories.HotelRepository;
import com.tech.project.AirbnbBackend.repositories.RoomRepository;
import com.tech.project.AirbnbBackend.services.HotelService;
import com.tech.project.AirbnbBackend.services.InventoryService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class HotelServiceImpl implements HotelService {

    private final HotelRepository hotelRepository;
    private final ModelMapper modelMapper;
    private final InventoryService inventoryService;
    private final RoomRepository roomRepository;

    @Override
    public HotelDto createNewHotel(HotelDto hotelDto) {

        log.info("Creating a new Hotel with name: {}",hotelDto.getName());

        Hotel hotel = modelMapper.map(hotelDto,Hotel.class);
        hotel.setActive(false);

        User user = (User) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();
        hotel.setOwner(user);
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

        User user = (User) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();
        assert user != null;
        log.info("user{}", user.getId());
        log.info("user{}", user.getName());
        log.info("user{}", user.getEmail());
        log.info("user{}", user.getRoles());
        if(!user.equals(hotel.getOwner())){
            throw new UnAuthorisedException("This user does not own this hotel with id: "+id);
        }
        return modelMapper.map(hotel,HotelDto.class);
    }

    @Override
    public HotelDto updateHotelById(Long id, HotelDto hotelDto) {
        Hotel hotel =  hotelRepository
                .findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Hotel not found with ID"+id));


        //just we have to assure the admin can update own hotel detail not other
        User user = (User) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();
        assert user != null;
        if(!user.equals(hotel.getOwner())){
            throw new UnAuthorisedException("This user does not own this hotel with id: "+id);
        }
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


        //just we have to assure the admin can delete own hotel detail not other
        User user = (User) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();
        assert user != null;
        if(!user.equals(hotel.getOwner())){
            throw new UnAuthorisedException("This user does not own this hotel with id: "+id);
        }

        // delete the future inventory for this hotel
        for (Room room: hotel.getRooms()){
            inventoryService.deleteAllInventories(room);
            roomRepository.deleteById(room.getId());
        }
        hotelRepository.deleteById(id);
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

        //just we have to assure the admin can update own hotel detail not other
        User user = (User) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();
        assert user != null;
        if(!user.equals(hotel.getOwner())){
            throw new UnAuthorisedException("This user does not own this hotel with id: "+hotelId);
        }
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

        //just we have to assure the admin can activate own hotel not other
        User user = (User) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();
        assert user != null;
        if(!user.equals(hotel.getOwner())){
            throw new UnAuthorisedException("This user does not own this hotel with id: "+hotelId);
        }
        hotel.setActive(true);
        //Create inventory for all the rooms for this hotel
        for (Room room : hotel.getRooms()){
            inventoryService.initializeRoomForAYear(room);
        }
        return modelMapper.map(hotel,HotelDto.class);
    }

    @Override
    public HotelInfoDto getHotelInfoById(Long hotelId) {
        Hotel hotel =  hotelRepository
                .findById(hotelId)
                .orElseThrow(()->new ResourceNotFoundException("Hotel not found with ID"+hotelId));

        List<RoomDto>rooms = hotel.getRooms()
                .stream().map((element) -> modelMapper.map(element, RoomDto.class))
                .toList();

        return new HotelInfoDto(modelMapper.map(hotel, HotelDto.class),rooms);
    }
}
