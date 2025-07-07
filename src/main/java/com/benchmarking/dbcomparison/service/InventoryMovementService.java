package com.benchmarking.dbcomparison.service;

import com.benchmarking.dbcomparison.model.InventoryMovement;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InventoryMovementService {
    InventoryMovement save(InventoryMovement movement);
    Optional<InventoryMovement> findById(UUID id);
    List<InventoryMovement> findAll();
    List<InventoryMovement> findByProductId(UUID productId);
    List<InventoryMovement> findByMovementType(String movementType);
    List<InventoryMovement> findByReference(String referenceType, UUID referenceId);
    void deleteById(UUID id);
}
