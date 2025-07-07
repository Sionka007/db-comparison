package com.benchmarking.dbcomparison.controller;

import com.benchmarking.dbcomparison.model.InventoryMovement;
import com.benchmarking.dbcomparison.service.InventoryMovementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/inventory")
public class InventoryMovementController {

    @Autowired
    private InventoryMovementService inventoryService;

    @GetMapping
    public List<InventoryMovement> getAll() {
        return inventoryService.findAll();
    }

    @GetMapping("/product/{productId}")
    public List<InventoryMovement> getByProduct(@PathVariable UUID productId) {
        return inventoryService.findByProductId(productId);
    }

    @GetMapping("/type/{movementType}")
    public List<InventoryMovement> getByMovementType(@PathVariable String movementType) {
        return inventoryService.findByMovementType(movementType);
    }

    @GetMapping("/reference/{type}/{id}")
    public List<InventoryMovement> getByReference(
            @PathVariable String type,
            @PathVariable UUID id) {
        return inventoryService.findByReference(type, id);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InventoryMovement> getById(@PathVariable UUID id) {
        Optional<InventoryMovement> movement = inventoryService.findById(id);
        return movement.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public InventoryMovement create(@RequestBody InventoryMovement movement) {
        return inventoryService.save(movement);
    }

    @PutMapping("/{id}")
    public ResponseEntity<InventoryMovement> update(
            @PathVariable UUID id,
            @RequestBody InventoryMovement movement) {
        if (!inventoryService.findById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        movement.setId(id);
        return ResponseEntity.ok(inventoryService.save(movement));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        if (!inventoryService.findById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        inventoryService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
