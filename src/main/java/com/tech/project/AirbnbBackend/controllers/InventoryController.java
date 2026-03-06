package com.tech.project.AirbnbBackend.controllers;

import com.tech.project.AirbnbBackend.dto.InventoryDto;
import com.tech.project.AirbnbBackend.dto.UpdateInventoryRequestDto;
import com.tech.project.AirbnbBackend.services.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/inventory")
@RequiredArgsConstructor
public class InventoryController {
    private final InventoryService inventoryService;

    @GetMapping("/room/{roomId}")
    public ResponseEntity<List<InventoryDto>> getAllInventoriesByRoom(@PathVariable Long roomId) {
        return ResponseEntity.ok(inventoryService.getAllInventoriesByRoom(roomId));
    }

    @PatchMapping("/room/{roomId}")
    public ResponseEntity<Void> updateInventory(@PathVariable Long roomId, @RequestBody UpdateInventoryRequestDto updateInventoryRequestDto) {
        inventoryService.updateInventory(roomId,updateInventoryRequestDto);
        return ResponseEntity.noContent().build();
    }
}
