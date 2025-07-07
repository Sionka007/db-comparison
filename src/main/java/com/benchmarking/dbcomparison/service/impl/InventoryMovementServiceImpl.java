package com.benchmarking.dbcomparison.service.impl;

import com.benchmarking.dbcomparison.model.InventoryMovement;
import com.benchmarking.dbcomparison.repository.InventoryMovementRepository;
import com.benchmarking.dbcomparison.service.InventoryMovementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class InventoryMovementServiceImpl implements InventoryMovementService {

    @Autowired
    private InventoryMovementRepository inventoryMovementRepository;

    @Override
    public InventoryMovement save(InventoryMovement movement) {
        return inventoryMovementRepository.save(movement);
    }

    @Override
    public Optional<InventoryMovement> findById(UUID id) {
        return inventoryMovementRepository.findById(id);
    }

    @Override
    public List<InventoryMovement> findAll() {
        return inventoryMovementRepository.findAll();
    }

    @Override
    public List<InventoryMovement> findByProductId(UUID productId) {
        return inventoryMovementRepository.findByProductId(productId);
    }

    @Override
    public List<InventoryMovement> findByMovementType(String movementType) {
        return inventoryMovementRepository.findByMovementType(movementType);
    }

    @Override
    public List<InventoryMovement> findByReference(String referenceType, UUID referenceId) {
        return inventoryMovementRepository.findByReferenceTypeAndReferenceId(referenceType, referenceId);
    }

    @Override
    public void deleteById(UUID id) {
        inventoryMovementRepository.deleteById(id);
    }
}
