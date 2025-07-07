package com.benchmarking.dbcomparison.service;

import com.benchmarking.dbcomparison.model.Customer;
import com.benchmarking.dbcomparison.model.Order;
import com.benchmarking.dbcomparison.model.OrderItem;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderItemService {
    OrderItem save(OrderItem orderItem);
    Optional<OrderItem> findById(UUID id);
    List<OrderItem> findAll();
    void deleteById(UUID id);
}
