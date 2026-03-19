package com.tech.project.AirbnbBackend.services;


import com.tech.project.AirbnbBackend.dto.HotelDto;
import com.tech.project.AirbnbBackend.dto.HotelInfoDto;
import com.tech.project.AirbnbBackend.dto.RoomDto;

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

    List<HotelDto>getAllHotels();

    List<RoomDto> getRoomsByHotelId(Long hotelId);
}
