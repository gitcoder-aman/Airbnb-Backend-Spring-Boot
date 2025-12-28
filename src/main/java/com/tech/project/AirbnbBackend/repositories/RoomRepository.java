package com.tech.project.AirbnbBackend.repositories;

import com.tech.project.AirbnbBackend.entities.Room;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<Room, Long> {
}