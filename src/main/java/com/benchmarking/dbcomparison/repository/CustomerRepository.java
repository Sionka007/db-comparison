package com.benchmarking.dbcomparison.repository;

import com.benchmarking.dbcomparison.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {
    Optional<Customer> findByEmail(String email);
    List<Customer> findByStatus(String status);
    List<Customer> findByNewsletterSubscription(Boolean subscribed);
    boolean existsByEmail(String email);
}
