package com.benchmarking.dbcomparison.service;

import com.benchmarking.dbcomparison.model.Customer;
import com.benchmarking.dbcomparison.model.Product;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductService {
    Product save(Product product);
    Optional<Product> findById(UUID id);
    List<Product> findAll();
    void deleteById(UUID id);
}
