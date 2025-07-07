package com.benchmarking.dbcomparison.repository;

import com.benchmarking.dbcomparison.model.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, UUID> {
    List<ProductCategory> findByParentCategoryId(UUID parentId);
    List<ProductCategory> findByLevel(Integer level);
    List<ProductCategory> findByIsActive(Boolean isActive);
    List<ProductCategory> findByParentCategoryIsNull();

    @Query("SELECT pc FROM ProductCategory pc WHERE pc.parentCategory IS NULL")
    List<ProductCategory> findAllMainCategories();
}
