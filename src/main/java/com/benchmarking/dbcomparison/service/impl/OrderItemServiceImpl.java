package com.benchmarking.dbcomparison.service.impl;

import com.benchmarking.dbcomparison.model.Customer;
import com.benchmarking.dbcomparison.model.OrderItem;
import com.benchmarking.dbcomparison.repository.OrderItemRepository;
import com.benchmarking.dbcomparison.service.OrderItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class OrderItemServiceImpl implements OrderItemService {

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Override
    public OrderItem save(OrderItem orderItem) {
        return orderItemRepository.save(orderItem);
    }

    @Override
    public Optional<OrderItem> findById(UUID id) {
        return orderItemRepository.findById(id);
    }

    @Override
    public List<OrderItem> findAll() {
        return orderItemRepository.findAll();
    }

    @Override
    public void deleteById(UUID id) {
        orderItemRepository.deleteById(id);
    }
}
