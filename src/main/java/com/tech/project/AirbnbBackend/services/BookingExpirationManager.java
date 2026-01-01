package com.tech.project.AirbnbBackend.services;

import com.tech.project.AirbnbBackend.entities.Booking;
import com.tech.project.AirbnbBackend.entities.enums.BookingStatus;
import com.tech.project.AirbnbBackend.repositories.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookingExpirationManager {

    private final BookingRepository bookingRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void doBookingStatusExpired(Long bookingId) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow();

        if (booking.getBookingStatus() == BookingStatus.EXPIRED) {
            return; // idempotent
        }

        booking.setBookingStatus(BookingStatus.EXPIRED);

        bookingRepository.save(booking);
    }
}
