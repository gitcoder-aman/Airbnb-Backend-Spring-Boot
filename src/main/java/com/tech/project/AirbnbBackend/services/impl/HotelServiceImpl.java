package com.tech.project.AirbnbBackend.services.impl;

import com.tech.project.AirbnbBackend.dto.HotelDto;
import com.tech.project.AirbnbBackend.dto.HotelInfoDto;
import com.tech.project.AirbnbBackend.dto.RoomDto;
import com.tech.project.AirbnbBackend.entities.Hotel;
import com.tech.project.AirbnbBackend.entities.Inventory;
import com.tech.project.AirbnbBackend.entities.Room;
import com.tech.project.AirbnbBackend.entities.User;
import com.tech.project.AirbnbBackend.exception.ResourceNotFoundException;
import com.tech.project.AirbnbBackend.exception.UnAuthorisedException;
import com.tech.project.AirbnbBackend.repositories.HotelMinPriceRepository;
import com.tech.project.AirbnbBackend.repositories.HotelRepository;
import com.tech.project.AirbnbBackend.repositories.InventoryRepository;
import com.tech.project.AirbnbBackend.repositories.RoomRepository;
import com.tech.project.AirbnbBackend.services.HotelService;
import com.tech.project.AirbnbBackend.services.InventoryService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.tech.project.AirbnbBackend.utils.AppUtils.getCurrentUser;


@Service
@RequiredArgsConstructor
@Slf4j
public class HotelServiceImpl implements HotelService {

    private static final Set<String> STOP_WORDS = Set.of(
            "hotel",
            "hotels",
            "resort",
            "resorts",
            "property",
            "stay",
            "accommodation"
    );
    private final HotelRepository hotelRepository;
    private final ModelMapper modelMapper;
    private final InventoryService inventoryService;
    private final RoomRepository roomRepository;
    private final InventoryRepository inventoryRepository;
    private final HotelMinPriceRepository hotelMinPriceRepository;

    @Override
    public HotelDto createNewHotel(HotelDto hotelDto) {

        log.info("Creating a new Hotel with name: {}", hotelDto.getName());

        Hotel hotel = modelMapper.map(hotelDto, Hotel.class);
        hotel.setActive(hotelDto.getActive() != null ? hotelDto.getActive() : true);

        User user = (User) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();
        hotel.setOwner(user);
//        hotel.setStartingPrice(BigDecimal.ZERO);
        hotel = hotelRepository.save(hotel);
        log.info("Created a new Hotel with Id: {}", hotel.getId());
        return modelMapper.map(hotel, HotelDto.class);
    }

    @Override
    public HotelDto getHotelById(Long id) {
        log.info("Getting the hotel with ID: {} ", id);
        Hotel hotel = hotelRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with ID" + id));

        User user = (User) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();
        assert user != null;
        log.info("user{}", user.getId());
        log.info("user{}", user.getName());
        log.info("user{}", user.getEmail());
        log.info("user{}", user.getRoles());
        if (!user.getId().equals(hotel.getOwner().getId())) {
            throw new UnAuthorisedException("This user does not own this hotel with id: " + id);
        }
        return modelMapper.map(hotel, HotelDto.class);
    }

    @Override
    public HotelDto updateHotelById(Long id, HotelDto hotelDto) {
        Hotel hotel = hotelRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with ID" + id));

        //just we have to assure the admin can update own hotel detail not other
        User user = (User) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();
        assert user != null;
        if (!user.getId().equals(hotel.getOwner().getId())) {
            throw new UnAuthorisedException("This user does not own this hotel with id: " + id);
        }

        Long originalId = hotel.getId();
        User originalOwner = hotel.getOwner();

        modelMapper.map(hotelDto, hotel);

        hotel.setId(originalId);
        hotel.setOwner(originalOwner);

        hotel = hotelRepository.save(hotel);
        return modelMapper.map(hotel, HotelDto.class);
    }

    @Transactional
    @Override
    public void deleteHotelById(Long id) {
        Hotel hotel = hotelRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with ID" + id));


        //just we have to assure the admin can delete own hotel detail not other
        User user = getCurrentUser();
        assert user != null;
        if (!user.getId().equals(hotel.getOwner().getId())) {
            throw new UnAuthorisedException("This user does not own this hotel with id: " + id);
        }

        // delete the future inventory for this hotel
        for (Room room : hotel.getRooms()) {
            inventoryService.deleteAllInventories(room);
            roomRepository.deleteById(room.getId());
            hotelMinPriceRepository.deleteByHotelId(hotel.getId());
        }
        hotelRepository.deleteById(id);
    }

    @Override
    public HotelDto updateParticularFieldById(Long hotelId, HotelDto hotelDto) {
        Hotel hotel = hotelRepository
                .findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with ID" + hotelId));
//        updates.forEach((field,value)->{
//            Field fieldToBeUpdated = ReflectionUtils.findField(Hotel.class,field);
//            assert fieldToBeUpdated != null;
//            fieldToBeUpdated.setAccessible(true);
//            ReflectionUtils.setField(fieldToBeUpdated,hotel,value);
//        });  //TODO: This Reflection use is not best practices

        //just we have to assure the admin can update own hotel detail not other
        User user = getCurrentUser();
        assert user != null;
        if (!user.getId().equals(hotel.getOwner().getId())) {
            throw new UnAuthorisedException("This user does not own this hotel with id: " + hotelId);
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
        if (hotelDto.getActive() != null) {
            hotel.setActive(hotelDto.getActive());
        }
        if (hotelDto.getPhotos().length > 0) {
            hotel.setPhotos(hotelDto.getPhotos());
        }
        if (hotelDto.getAmenities().length > 0) {
            hotel.setAmenities(hotelDto.getAmenities());
        }
        if (hotelDto.getContactInfo().getEmail() != null) {
            hotel.getContactInfo().setAddress(hotelDto.getContactInfo().getEmail());
        }
        if (hotelDto.getContactInfo().getLocation() != null) {
            hotel.getContactInfo().setLocation(hotelDto.getContactInfo().getLocation());
        }
        if (hotelDto.getContactInfo().getPhoneNumber() != null) {
            hotel.getContactInfo().setPhoneNumber(hotelDto.getContactInfo().getPhoneNumber());
        }

        var updatedHotelField = hotelRepository.save(hotel);
        return modelMapper.map(updatedHotelField, HotelDto.class);
    }

    @Override
    @Transactional
    public HotelDto activateHotel(Long hotelId) {
        log.info("Activating the hotel with ID: {}", hotelId);
        Hotel hotel = hotelRepository
                .findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with ID" + hotelId));

        //just we have to assure the admin can activate own hotel not other
        User user = getCurrentUser();
        assert user != null;
        if (!user.getId().equals(hotel.getOwner().getId())) {
            throw new UnAuthorisedException("This user does not own this hotel with id: " + hotelId);
        }
        hotel.setActive(true);
        //Create inventory for all the rooms for this hotel
        for (Room room : hotel.getRooms()) {
            inventoryService.initializeRoomForAYear(room);
        }
        return modelMapper.map(hotel, HotelDto.class);
    }

    @Override
    public HotelInfoDto getHotelInfoById(Long hotelId) {
        Hotel hotel = hotelRepository
                .findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with ID" + hotelId));

        List<RoomDto> rooms = hotel.getRooms()
                .stream().map((element) -> modelMapper.map(element, RoomDto.class))
                .toList();

        return new HotelInfoDto(modelMapper.map(hotel, HotelDto.class), rooms);
    }

    @Override
    public List<HotelDto> getAllHotelsByOwner() {
        User user = getCurrentUser();
        log.info("Getting all hotels for this admin user with id :{}", user.getId());
        List<Hotel> hotels = hotelRepository.findByOwner(user);
        return hotels.stream().map(
                (hotel) -> modelMapper.map(hotel, HotelDto.class)
        ).collect(Collectors.toList());
    }

    @Override
    public Page<HotelDto> getAllHotels(Integer page, Integer size) {


        Pageable pageable = PageRequest.of(page, size);

        log.info("Getting all active hotels");

        return hotelRepository.findByActiveTrue(pageable)
                .map(hotel -> modelMapper.map(hotel, HotelDto.class));
    }
    @Override
    public List<RoomDto> getRoomsByHotelId(
            Long hotelId,
            LocalDate checkInDate,
            LocalDate checkOutDate) {

        if (checkInDate.isAfter(checkOutDate)
                || checkInDate.isEqual(checkOutDate)) {
            throw new IllegalArgumentException(
                    "Check-out date must be after check-in date");
        }

        Hotel hotel = hotelRepository
                .findById(hotelId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Hotel not found with ID " + hotelId));

        List<RoomDto> rooms = new ArrayList<>();

        for (Room room : hotel.getRooms()) {

            RoomDto dto = modelMapper.map(room, RoomDto.class);

            List<Inventory> inventories =
                    inventoryRepository.findInventoriesBetweenDates(
                            hotelId,
                            room.getId(),
                            checkInDate,
                            checkOutDate);

            BigDecimal totalPrice = inventories.stream()
                    .map(Inventory::getPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            dto.setTotalPrice(totalPrice);

            rooms.add(dto);
        }

        return rooms;
    }

    @Override
    public List<Hotel> getHotelByHotelName(String hotelName) {

        String cleanedKeyword = Arrays.stream(hotelName.split("\\s+"))
                .filter(word -> !STOP_WORDS.contains(word.toLowerCase()))
                .collect(Collectors.joining(" "));

        List<Hotel> hotels = new ArrayList<>();

        for(String keyword : cleanedKeyword.split("\\s+")){
            hotels.addAll(
                    hotelRepository.searchByKeyword(keyword)
            );
        }
        return hotels.stream()
                .distinct()
                .toList();
    }
}
