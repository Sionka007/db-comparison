package com.benchmarking.dbcomparison.service.impl;

import com.benchmarking.dbcomparison.model.ProductCategory;
import com.benchmarking.dbcomparison.repository.ProductCategoryRepository;
import com.benchmarking.dbcomparison.service.ProductCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProductCategoryServiceImpl implements ProductCategoryService {

    @Autowired
    private ProductCategoryRepository productCategoryRepository;

    @Override
    public ProductCategory save(ProductCategory category) {
        return productCategoryRepository.save(category);
    }

    @Override
    public Optional<ProductCategory> findById(UUID id) {
        return productCategoryRepository.findById(id);
    }

    @Override
    public List<ProductCategory> findAll() {
        return productCategoryRepository.findAll();
    }

    @Override
    public List<ProductCategory> findRootCategories() {
        return productCategoryRepository.findByParentCategoryIsNull();
    }

    @Override
    public List<ProductCategory> findSubcategories(UUID parentId) {
        return productCategoryRepository.findByParentCategoryId(parentId);
    }

    @Override
    public void deleteById(UUID id) {
        productCategoryRepository.deleteById(id);
    }
}
