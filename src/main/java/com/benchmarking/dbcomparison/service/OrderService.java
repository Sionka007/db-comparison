package com.benchmarking.dbcomparison.service;

import com.benchmarking.dbcomparison.model.Customer;
import com.benchmarking.dbcomparison.model.Order;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderService {
    Order save(Order order);
    Optional<Order> findById(UUID id);
    List<Order> findAll();
    void deleteById(UUID id);
}
