package com.benchmarking.dbcomparison.repository;

import com.benchmarking.dbcomparison.model.InventoryMovement;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Repository
public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, UUID> {

    List<InventoryMovement> findByProductId(UUID productId);
    List<InventoryMovement> findByMovementType(String movementType);
    List<InventoryMovement> findByReferenceTypeAndReferenceId(String referenceType, UUID referenceId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query(value = """
    DELETE FROM inventory_movement
    WHERE id IN (
      SELECT id FROM (
        SELECT id FROM inventory_movement ORDER BY id LIMIT :limit
      ) t
    )
    """, nativeQuery = true)
    int deleteTopNMySql(@Param("limit") int limit);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query(value = """
    WITH picked AS (
      SELECT ctid
      FROM inventory_movement
      ORDER BY id
      LIMIT :limit
      FOR UPDATE SKIP LOCKED
    )
    DELETE FROM inventory_movement im
    USING picked p
    WHERE im.ctid = p.ctid
    """, nativeQuery = true)
    int deleteTopNPostgres(@Param("limit") int limit);

}
