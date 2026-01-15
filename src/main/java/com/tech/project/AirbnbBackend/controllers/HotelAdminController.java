package com.tech.project.AirbnbBackend.controllers;

import com.tech.project.AirbnbBackend.dto.HotelDto;
import com.tech.project.AirbnbBackend.services.HotelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping(path = "/admin/hotels")
@RequiredArgsConstructor
@Slf4j
public class HotelAdminController {
    private final HotelService hotelService;

    @PostMapping
    public ResponseEntity<HotelDto>createNewHotel(@Valid @RequestBody HotelDto hotelDto){
        log.info("Attempting to create a new Hotel with name: {}", hotelDto.getName());
        HotelDto createdHotelDto = hotelService.createNewHotel(hotelDto);
        return new ResponseEntity<>(createdHotelDto, HttpStatus.CREATED);
    }

    @GetMapping("/{hotelId}")
    public ResponseEntity<HotelDto>getHotelById(@PathVariable Long hotelId){
        log.info("Getting to a Hotel with id : {}", hotelId);
        HotelDto responseHotelDto = hotelService.getHotelById(hotelId);
        return new ResponseEntity<>(responseHotelDto,HttpStatus.OK);
    }

    @PutMapping("/{hotelId}")
    public ResponseEntity<HotelDto>updateHotelById(@PathVariable Long hotelId,@Valid @RequestBody HotelDto hotelDto){
        HotelDto updatedHotelDtoResponse = hotelService.updateHotelById(hotelId,hotelDto);
        return new ResponseEntity<>(updatedHotelDtoResponse,HttpStatus.OK);
    }

    @DeleteMapping("/{hotelId}")
    public ResponseEntity<Void>deleteHotelById(@PathVariable Long hotelId){
        hotelService.deleteHotelById(hotelId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{hotelId}")
    public ResponseEntity<HotelDto>updateParticularField(@PathVariable Long hotelId,@Valid @RequestBody HotelDto hotelDto){
        HotelDto updatedHotelDtoResponse = hotelService.updateParticularFieldById(hotelId,hotelDto);
        return new ResponseEntity<>(updatedHotelDtoResponse,HttpStatus.OK);
    }

    @PatchMapping("/activate/{hotelId}")
    public ResponseEntity<HotelDto>activateHotelById(@PathVariable Long hotelId){
        HotelDto activateHotelResponse = hotelService.activateHotel(hotelId);
        return ResponseEntity.ok(activateHotelResponse);
    }
}
