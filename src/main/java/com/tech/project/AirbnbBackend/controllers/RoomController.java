package com.tech.project.AirbnbBackend.controllers;

import com.tech.project.AirbnbBackend.dto.RoomDto;
import com.tech.project.AirbnbBackend.services.RoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/admin/hotels/{hotelId}/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @PostMapping
    public ResponseEntity<RoomDto>createNewRoomInHotel(@PathVariable Long hotelId, @Valid @RequestBody RoomDto roomDto){

        RoomDto createRoomResponse = roomService.createNewRoomInHotel(hotelId,roomDto);
        return new ResponseEntity<>(createRoomResponse, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<RoomDto>>getAllRoomsInHotel(@PathVariable Long hotelId){
        List<RoomDto> allRoomInHotelByIdResponse = roomService.getAllRoomInHotelById(hotelId);
        return ResponseEntity.ok(allRoomInHotelByIdResponse);
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<RoomDto>getRoomById(@PathVariable Long hotelId,@PathVariable Long roomId){
        RoomDto roomByIdResponse = roomService.getRoomById(roomId);
        return ResponseEntity.ok(roomByIdResponse);
    }

    @DeleteMapping("/{roomId}")
    public ResponseEntity<Void>deleteRoomById(@PathVariable Long hotelId,@PathVariable Long roomId){
        roomService.deleteRoomById(roomId);
        return ResponseEntity.noContent().build();
    }
    @PutMapping("/{roomId}")
    public ResponseEntity<RoomDto>updateRoomById(@PathVariable Long hotelId,@PathVariable Long roomId,@Valid @RequestBody RoomDto roomDto){
        RoomDto updatedRoomResponse = roomService.updateRoomById(roomId, roomDto);
        return ResponseEntity.ok(updatedRoomResponse);
    }
}
