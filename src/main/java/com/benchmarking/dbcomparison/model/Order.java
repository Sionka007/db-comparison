package com.benchmarking.dbcomparison.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Column(unique = true)
    private String orderNumber;

    private String status;
    private BigDecimal totalAmount;
    private String shippingAddressStreet;
    private String shippingAddressCity;
    private String shippingAddressPostalCode;
    private String shippingAddressCountry;
    private String shippingMethod;
    private BigDecimal shippingCost;
    private String paymentMethod;
    private String paymentStatus;
    private String discountCode;
    private BigDecimal discountAmount;
    private BigDecimal taxAmount;
    private String notes;
    private LocalDate estimatedDeliveryDate;
    private LocalDate actualDeliveryDate;
    private String trackingNumber;
    private LocalDateTime orderDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();
}
