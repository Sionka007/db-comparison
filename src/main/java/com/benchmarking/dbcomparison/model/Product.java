package com.benchmarking.dbcomparison.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;
    private String description;
    private BigDecimal price;
    private Integer stockQuantity;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private ProductCategory category;

    @ManyToOne
    @JoinColumn(name = "brand_id")
    private Brand brand;

    private BigDecimal weight;
    private String dimensions;

    @Column(unique = true)
    private String sku;

    private String barcode;
    private Boolean isAvailable = true;
    private Integer minStockLevel;
    private Integer maxStockLevel;
    private BigDecimal rating;
    private Integer reviewCount = 0;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
