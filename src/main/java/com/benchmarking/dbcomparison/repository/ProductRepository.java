package com.benchmarking.dbcomparison.repository;

import com.benchmarking.dbcomparison.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {
    List<Product> findTop10ByOrderByRatingDesc();

    List<Product> findTop100ByOrderByRatingDesc();
}
