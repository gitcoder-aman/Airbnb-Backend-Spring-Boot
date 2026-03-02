package com.tech.project.AirbnbBackend.repositories;

import com.tech.project.AirbnbBackend.entities.Booking;
import com.tech.project.AirbnbBackend.entities.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    Optional<Booking> findByPaymentSessionId(String sessionId);

    List<Booking> findByHotel(Hotel hotel);
}