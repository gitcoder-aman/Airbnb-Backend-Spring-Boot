package com.tech.project.AirbnbBackend.controllers.user;

import com.tech.project.AirbnbBackend.dto.*;
import com.tech.project.AirbnbBackend.services.HotelService;
import com.tech.project.AirbnbBackend.services.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping(path = "/api/v1/hotels")
@RestController
@RequiredArgsConstructor
public class HotelBrowseController {

    private final InventoryService inventoryService;
    private final HotelService hotelService;

    @GetMapping("/search")
    public ResponseEntity<Page<HotelPriceDto>>searchHotels(@Valid @RequestBody HotelSearchRequest hotelSearchRequest){
        Page<HotelPriceDto> page = inventoryService.searchHotels(hotelSearchRequest);
        return ResponseEntity.ok(page);
    }
    @GetMapping("/{hotelId}/info")
    public ResponseEntity<HotelInfoDto>getHotelInfo(@PathVariable Long hotelId){
        HotelInfoDto hotelInfoByIdResponse = hotelService.getHotelInfoById(hotelId);
        return ResponseEntity.ok(hotelInfoByIdResponse);
    }
    @GetMapping
    public ResponseEntity<List<HotelDto>> getAllHotels() {
        return ResponseEntity.ok(hotelService.getAllHotels());
    }
    //  Get rooms of a hotel
    @GetMapping("/{hotelId}/rooms")
    public ResponseEntity<List<RoomDto>> getRoomsByHotel(@PathVariable Long hotelId) {
        return ResponseEntity.ok(hotelService.getRoomsByHotelId(hotelId));
    }


}
