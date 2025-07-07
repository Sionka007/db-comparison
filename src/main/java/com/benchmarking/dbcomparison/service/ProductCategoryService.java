package com.benchmarking.dbcomparison.service;

import com.benchmarking.dbcomparison.model.ProductCategory;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductCategoryService {
    ProductCategory save(ProductCategory category);
    Optional<ProductCategory> findById(UUID id);
    List<ProductCategory> findAll();
    List<ProductCategory> findRootCategories();
    List<ProductCategory> findSubcategories(UUID parentId);
    void deleteById(UUID id);
}
