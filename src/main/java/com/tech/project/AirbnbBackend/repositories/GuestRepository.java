package com.tech.project.AirbnbBackend.repositories;

import com.tech.project.AirbnbBackend.entities.Guest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GuestRepository extends JpaRepository<Guest, Long> {
}