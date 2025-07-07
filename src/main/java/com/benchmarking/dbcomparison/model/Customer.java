package com.benchmarking.dbcomparison.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String firstName;
    private String lastName;

    @Column(unique = true, nullable = false)
    private String email;

    private String phoneNumber;
    private LocalDate dateOfBirth;
    private String addressStreet;
    private String addressCity;
    private String addressPostalCode;
    private String addressCountry;
    private String status = "ACTIVE";
    private Integer loyaltyPoints = 0;
    private Boolean newsletterSubscription = false;
    private LocalDateTime lastLoginDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
