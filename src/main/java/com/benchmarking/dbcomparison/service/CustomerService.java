package com.benchmarking.dbcomparison.service;

import com.benchmarking.dbcomparison.model.Customer;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerService {
    Customer save(Customer customer);
    Optional<Customer> findById(UUID id);
    List<Customer> findAll();
    void deleteById(UUID id);
}
