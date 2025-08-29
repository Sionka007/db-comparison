package com.benchmarking.dbcomparison.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Entity
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(length = 255)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    private Integer stockQuantity;

    @ManyToOne
    @JoinColumn(name = "category_id")
    @OnDelete(action = OnDeleteAction.CASCADE) // DB-level, gdy Hibernate tworzy FK
    private ProductCategory category;

    @ManyToOne
    @JoinColumn(name = "brand_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Brand brand;

    @Column(precision = 10, scale = 2)
    private BigDecimal weight;

    @Column(length = 50)
    private String dimensions;

    @Column(unique = true, length = 50)
    private String sku;

    @Column(length = 50)
    private String barcode;

    private Boolean isAvailable = true;
    private Integer minStockLevel;
    private Integer maxStockLevel;
    private BigDecimal rating;
    private Integer reviewCount = 0;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Recenzje produktu
    @OneToMany(mappedBy = "product", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<ProductReview> reviews = new ArrayList<>();

    // Ruchy magazynowe produktu
    @OneToMany(mappedBy = "product", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<InventoryMovement> movements = new ArrayList<>();

    // Pozycje zamówień z tym produktem
    @OneToMany(mappedBy = "product", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();
}
