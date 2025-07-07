package com.benchmarking.dbcomparison.service;

import com.benchmarking.dbcomparison.model.ProductReview;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductReviewService {
    ProductReview save(ProductReview review);
    Optional<ProductReview> findById(UUID id);
    List<ProductReview> findAll();
    List<ProductReview> findByProductId(UUID productId);
    List<ProductReview> findByCustomerId(UUID customerId);
    List<ProductReview> findByProductIdAndMinRating(UUID productId, Integer minRating);
    void deleteById(UUID id);
}
