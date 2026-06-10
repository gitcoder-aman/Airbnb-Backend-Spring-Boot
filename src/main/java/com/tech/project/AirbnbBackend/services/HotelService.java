package com.tech.project.AirbnbBackend.services;


import com.tech.project.AirbnbBackend.dto.HotelDto;
import com.tech.project.AirbnbBackend.dto.HotelInfoDto;
import com.tech.project.AirbnbBackend.dto.RoomDto;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface HotelService {

    HotelDto createNewHotel(HotelDto hotelDto);

    HotelDto getHotelById(Long id);

    HotelDto updateHotelById(Long id,HotelDto hotelDto);

    void deleteHotelById(Long id);

    HotelDto updateParticularFieldById(Long hotelId, HotelDto hotelDto);

    HotelDto activateHotel(Long hotelId);

    HotelInfoDto getHotelInfoById(Long hotelId);

    List<HotelDto> getAllHotelsByOwner();

    Page<HotelDto> getAllHotels(Integer page, Integer size);

    List<RoomDto> getRoomsByHotelId(Long hotelId, LocalDate checkInDate, LocalDate checkOutDate);
}
