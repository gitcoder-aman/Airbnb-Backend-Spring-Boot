package com.tech.project.AirbnbBackend.services;

import com.tech.project.AirbnbBackend.entities.Room;

public interface InventoryService {

    void initializeRoomForAYear(Room room);

    void deleteFutureInventory(Room room);

}
