package com.benchmarking.dbcomparison.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
public class InventoryMovement {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    private String movementType;
    private Integer quantity;
    private String referenceType;
    private UUID referenceId;
    private String notes;
    private LocalDateTime createdAt;
    private UUID createdBy;
}
