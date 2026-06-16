package com.tech.project.AirbnbBackend.repositories;

import com.tech.project.AirbnbBackend.entities.Hotel;
import com.tech.project.AirbnbBackend.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;


public interface HotelRepository extends JpaRepository<Hotel, Long> {
    List<Hotel> findByOwner(User user);

    Page<Hotel> findByActiveTrue(Pageable pageable);

//    Optional<Hotel> findByNameContainingIgnoreCase(String hotelName);

    @Query("""
            SELECT h
            FROM Hotel h
            WHERE LOWER(h.name) LIKE LOWER(CONCAT('%', :keyword, '%')) AND h.active=true
            """)
    List<Hotel> searchByKeyword(@Param("keyword") String keyword);
}