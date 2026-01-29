package com.tech.project.AirbnbBackend.controllers;

import com.tech.project.AirbnbBackend.dto.BookingDto;
import com.tech.project.AirbnbBackend.dto.BookingRequest;
import com.tech.project.AirbnbBackend.dto.GuestDto;
import com.tech.project.AirbnbBackend.services.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class HotelBookingController {

    private final BookingService bookingService;

    @PostMapping("/init")
    public ResponseEntity<BookingDto>initialiseBooking(@Valid @RequestBody BookingRequest bookingRequest){
        BookingDto bookingResponse = bookingService.initialiseBooking(bookingRequest);
        return ResponseEntity.ok(bookingResponse);
    }

    @PostMapping("{bookingId}/addGuests")
    public ResponseEntity<BookingDto> addGuests(@PathVariable Long bookingId,@Valid @RequestBody List<GuestDto> guestDtoList){
        BookingDto bookingAddGuestResponse = bookingService.addGuests(bookingId,guestDtoList);
        return ResponseEntity.ok(bookingAddGuestResponse);
    }
}
