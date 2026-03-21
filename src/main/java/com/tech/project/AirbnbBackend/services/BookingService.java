package com.tech.project.AirbnbBackend.services;


import com.stripe.model.Event;
import com.tech.project.AirbnbBackend.dto.BookingDto;
import com.tech.project.AirbnbBackend.dto.BookingRequest;
import com.tech.project.AirbnbBackend.dto.GuestDto;
import com.tech.project.AirbnbBackend.dto.HotelReportDto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface BookingService {


    BookingDto initialiseBooking(BookingRequest bookingRequest);

    BookingDto addGuests(Long bookingId, List<GuestDto> guestDtoList);

    String initiatePayment(Long bookingId);

    void capturePayment(Event event);

    void cancelBooking(Long bookingId);

    String getBookingStatus(Long bookingId);

    List<BookingDto> getAllBookingsByHotelId(Long hotelId);

    HotelReportDto getHotelReport(Long hotelId, LocalDate startDate, LocalDate endDate);

    List<BookingDto> getMyBookings();

    void updateBookingStatusInCheckIn(Long bookingId);

}
