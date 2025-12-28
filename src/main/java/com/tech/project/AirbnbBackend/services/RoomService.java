package com.tech.project.AirbnbBackend.services;


import com.tech.project.AirbnbBackend.dto.RoomDto;

import java.util.List;

public interface RoomService {

    RoomDto createNewRoomInHotel(Long hotelId, RoomDto roomDto);

    List<RoomDto>getAllRoomInHotelById(Long hotelId);

    RoomDto getRoomById(Long roomId);

    void deleteRoomById(Long roomId);

    RoomDto updateRoomById(Long roomId,RoomDto roomDto);
}
