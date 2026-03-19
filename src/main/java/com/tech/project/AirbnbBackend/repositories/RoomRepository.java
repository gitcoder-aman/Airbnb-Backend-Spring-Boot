package com.tech.project.AirbnbBackend.repositories;

import com.tech.project.AirbnbBackend.entities.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {
    @Query("SELECT MIN(r.basePrice) FROM Room r WHERE r.hotel.id = :hotelId")
    BigDecimal findMinPriceByHotelId(@Param("hotelId") Long hotelId);
}