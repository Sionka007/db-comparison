package com.benchmarking.dbcomparison.repository;

import com.benchmarking.dbcomparison.model.ProductCategory;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Repository
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, UUID> {
    List<ProductCategory> findByParentCategoryId(UUID parentId);
    List<ProductCategory> findByLevel(Integer level);
    List<ProductCategory> findByIsActive(Boolean isActive);
    List<ProductCategory> findByParentCategoryIsNull();

    @Query("SELECT pc FROM ProductCategory pc WHERE pc.parentCategory IS NULL")
    List<ProductCategory> findAllMainCategories();

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query(value = """
    DELETE FROM product_category
    WHERE id IN (
      SELECT id FROM (
        SELECT id FROM product_category ORDER BY id LIMIT :limit
      ) t
    )
    """, nativeQuery = true)
    int deleteTopNMySql(@Param("limit") int limit);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query(value = """
    WITH picked AS (
      SELECT ctid
      FROM product_category
      ORDER BY id
      LIMIT :limit
      FOR UPDATE SKIP LOCKED
    )
    DELETE FROM product_category pc
    USING picked p
    WHERE pc.ctid = p.ctid
    """, nativeQuery = true)
    int deleteTopNPostgres(@Param("limit") int limit);

}
