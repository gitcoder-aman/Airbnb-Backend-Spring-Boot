package com.tech.project.AirbnbBackend.repositories;

import com.tech.project.AirbnbBackend.dto.HotelDto;
import com.tech.project.AirbnbBackend.entities.Hotel;
import com.tech.project.AirbnbBackend.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HotelRepository extends JpaRepository<Hotel, Long> {
    List<Hotel> findByOwner(User user);

    List<Hotel> findByActiveTrue();

}