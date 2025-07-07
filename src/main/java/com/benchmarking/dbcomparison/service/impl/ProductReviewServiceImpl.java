package com.benchmarking.dbcomparison.service.impl;

import com.benchmarking.dbcomparison.model.ProductReview;
import com.benchmarking.dbcomparison.repository.ProductReviewRepository;
import com.benchmarking.dbcomparison.service.ProductReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProductReviewServiceImpl implements ProductReviewService {

    @Autowired
    private ProductReviewRepository productReviewRepository;

    @Override
    public ProductReview save(ProductReview review) {
        return productReviewRepository.save(review);
    }

    @Override
    public Optional<ProductReview> findById(UUID id) {
        return productReviewRepository.findById(id);
    }

    @Override
    public List<ProductReview> findAll() {
        return productReviewRepository.findAll();
    }

    @Override
    public List<ProductReview> findByProductId(UUID productId) {
        return productReviewRepository.findByProductId(productId);
    }

    @Override
    public List<ProductReview> findByCustomerId(UUID customerId) {
        return productReviewRepository.findByCustomerId(customerId);
    }

    @Override
    public List<ProductReview> findByProductIdAndMinRating(UUID productId, Integer minRating) {
        return productReviewRepository.findByProductIdAndRatingGreaterThanEqual(productId, minRating);
    }

    @Override
    public void deleteById(UUID id) {
        productReviewRepository.deleteById(id);
    }
}
