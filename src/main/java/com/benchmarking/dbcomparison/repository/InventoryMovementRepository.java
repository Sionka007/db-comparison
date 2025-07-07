package com.benchmarking.dbcomparison.repository;

import com.benchmarking.dbcomparison.model.InventoryMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, UUID> {
    List<InventoryMovement> findByProductId(UUID productId);
    List<InventoryMovement> findByMovementType(String movementType);
    List<InventoryMovement> findByReferenceTypeAndReferenceId(String referenceType, UUID referenceId);
}
