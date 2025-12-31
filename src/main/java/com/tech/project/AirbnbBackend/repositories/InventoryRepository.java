package com.tech.project.AirbnbBackend.repositories;

import com.tech.project.AirbnbBackend.entities.Hotel;
import com.tech.project.AirbnbBackend.entities.Inventory;
import com.tech.project.AirbnbBackend.entities.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDate;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    void deleteByRoom(Room room);

    //Fetches all distinct hotels in a city that can provide the required number
    // of rooms for every day in the given date range.
    @Query("""
            SELECT DISTINCT i.hotel
            FROM Inventory i WHERE i.city = :city AND i.date BETWEEN :startDate AND :endDate
                        AND i.closed = FALSE AND  (i.totalCount-i.bookCount) >= :numberOfRooms
                        GROUP BY i.hotel,i.room HAVING COUNT(i.date)=:dateCount
            """)
    Page<Hotel>findHotelsWithAvailableInventory(
            @Param("city") String city,
            @Param("startDate")LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("numberOfRooms") Integer numberOfRooms,
            @Param("dateCount") Long dateCount,
            Pageable pageable
            );
}