package com.benchmarking.dbcomparison.service.impl;

import com.benchmarking.dbcomparison.model.Customer;
import com.benchmarking.dbcomparison.model.Product;
import com.benchmarking.dbcomparison.repository.ProductRepository;
import com.benchmarking.dbcomparison.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Override
    public Product save(Product product) {
        return productRepository.save(product);
    }

    @Override
    public Optional<Product> findById(UUID id) {
        return productRepository.findById(id);
    }

    @Override
    public List<Product> findAll() {
        return productRepository.findAll();
    }

    @Override
    public void deleteById(UUID id) {
        productRepository.deleteById(id);
    }
}
