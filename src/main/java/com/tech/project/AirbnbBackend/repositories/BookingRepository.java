package com.tech.project.AirbnbBackend.repositories;

import com.tech.project.AirbnbBackend.entities.Booking;
import com.tech.project.AirbnbBackend.entities.Hotel;
import com.tech.project.AirbnbBackend.entities.Room;
import com.tech.project.AirbnbBackend.entities.User;
import com.tech.project.AirbnbBackend.entities.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    Optional<Booking> findByPaymentSessionId(String sessionId);

    List<Booking> findByHotel(Hotel hotel);

    List<Booking>findByHotelAndCreatedAtBetween(Hotel hotel, LocalDateTime startDate, LocalDateTime endDate);

    @Query("""
            SELECT b
            FROM Booking b
            JOIN b.hotel h
            WHERE h.owner = :owner
            ORDER BY b.createdAt DESC, b.id DESC
            """)
    List<Booking> findByHotelOwnerOrderByCreatedAtDesc(@Param("owner") User owner);

    List<Booking>findByUser(User user);

    List<Booking> findByBookingStatusAndCheckOutDateBefore(BookingStatus bookingStatus, LocalDate now);

    boolean existsByUserAndRoomAndBookingStatus(User user, Room room, BookingStatus bookingStatus);

    boolean existsByUserAndRoomAndBookingStatusIn(User user, Room room, List<BookingStatus> bookingStatuses);


    List<Booking> findByBookingStatusInAndCreatedAtBefore(List<BookingStatus> reserved, LocalDateTime expiryTime);
}