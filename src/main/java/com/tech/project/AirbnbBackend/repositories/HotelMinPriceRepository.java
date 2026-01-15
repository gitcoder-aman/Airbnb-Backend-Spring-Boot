package com.tech.project.AirbnbBackend.repositories;

import com.tech.project.AirbnbBackend.dto.HotelPriceDto;
import com.tech.project.AirbnbBackend.entities.Hotel;
import com.tech.project.AirbnbBackend.entities.HotelMinPrice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.Optional;

public interface HotelMinPriceRepository extends JpaRepository<HotelMinPrice,Long> {

//    reject hotel if ANY date inventory is closed in AND Not Exists
    @Query("""
            SELECT new com.tech.project.AirbnbBackend.dto.HotelPriceDto(hmp.hotel,AVG(hmp.price))
                        FROM HotelMinPrice hmp WHERE hmp.hotel.city = :city
                                    AND hmp.hotel.active = TRUE
                                    AND hmp.date BETWEEN :checkInDate AND :checkOutDate
                                    AND NOT EXISTS(
                                                SELECT 1 FROM Inventory inv
                                                            WHERE inv.hotel=hmp.hotel
                                                                        AND inv.date BETWEEN :checkInDate AND :checkOutDate
                                                                                    AND inv.closed = TRUE
                                                )
                                                              AND NOT EXISTS (
                                                                    SELECT 1
                                                                    FROM Inventory inv
                                                                    WHERE inv.hotel = hmp.hotel
                                                                      AND inv.date BETWEEN :checkInDate AND :checkOutDate
                                                                      AND (inv.totalCount - inv.bookCount) < :numberOfRooms
                                                              )
                        GROUP BY hmp.hotel
            """)
    Page<HotelPriceDto> findHotelsWithAvailableInventory(
            @Param("city") String city,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate,
            @Param("numberOfRooms") Integer numberOfRooms,
            @Param("dateCount") Long dateCount,
            Pageable pageable
    );

    Optional<HotelMinPrice> findByHotelAndDate(Hotel hotel, LocalDate date);
}
