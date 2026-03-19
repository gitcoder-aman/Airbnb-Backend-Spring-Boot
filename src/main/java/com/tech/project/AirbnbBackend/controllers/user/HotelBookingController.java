package com.tech.project.AirbnbBackend.controllers.user;

import com.tech.project.AirbnbBackend.dto.BookingDto;
import com.tech.project.AirbnbBackend.dto.BookingRequest;
import com.tech.project.AirbnbBackend.dto.GuestDto;
import com.tech.project.AirbnbBackend.services.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(path = "/api/v1/bookings")
@RequiredArgsConstructor
public class HotelBookingController {

    private final BookingService bookingService;

    @PostMapping("/init")
    public ResponseEntity<BookingDto>initialiseBooking(@Valid @RequestBody BookingRequest bookingRequest){
        BookingDto bookingResponse = bookingService.initialiseBooking(bookingRequest);
        return ResponseEntity.ok(bookingResponse);
    }

    @PostMapping("/{bookingId}/addGuests")
    public ResponseEntity<BookingDto> addGuests(@PathVariable Long bookingId,@Valid @RequestBody List<GuestDto> guestDtoList){
        BookingDto bookingAddGuestResponse = bookingService.addGuests(bookingId,guestDtoList);
        return ResponseEntity.ok(bookingAddGuestResponse);
    }

    @PostMapping("/{bookingId}/payment")
    public ResponseEntity<Map<String,String>> initiatePayment(@PathVariable Long bookingId){
        String sessionUrl = bookingService.initiatePayment(bookingId);
        return ResponseEntity.ok(Map.of("sessionUrl",sessionUrl));
    }
    @PostMapping("/{bookingId}/cancel")
    public ResponseEntity<Void> cancelBooking(@PathVariable Long bookingId){
        bookingService.cancelBooking(bookingId);
        return ResponseEntity.noContent().build();
    }
    @PostMapping("/{bookingId}/status")
    public ResponseEntity<Map<String,String>> getBookingStatus(@PathVariable Long bookingId){
        return ResponseEntity.ok(Map.of("status",bookingService.getBookingStatus(bookingId)));
    }
}
