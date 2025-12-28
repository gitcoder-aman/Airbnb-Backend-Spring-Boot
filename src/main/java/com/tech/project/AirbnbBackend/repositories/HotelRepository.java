package com.tech.project.AirbnbBackend.repositories;

import com.tech.project.AirbnbBackend.entities.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HotelRepository extends JpaRepository<Hotel, Long> {
}