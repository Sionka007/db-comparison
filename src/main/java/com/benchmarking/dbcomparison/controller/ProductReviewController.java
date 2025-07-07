package com.benchmarking.dbcomparison.controller;

import com.benchmarking.dbcomparison.model.ProductReview;
import com.benchmarking.dbcomparison.service.ProductReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/reviews")
public class ProductReviewController {

    @Autowired
    private ProductReviewService reviewService;

    @GetMapping
    public List<ProductReview> getAll() {
        return reviewService.findAll();
    }

    @GetMapping("/product/{productId}")
    public List<ProductReview> getByProduct(@PathVariable UUID productId) {
        return reviewService.findByProductId(productId);
    }

    @GetMapping("/customer/{customerId}")
    public List<ProductReview> getByCustomer(@PathVariable UUID customerId) {
        return reviewService.findByCustomerId(customerId);
    }

    @GetMapping("/product/{productId}/rating/{minRating}")
    public List<ProductReview> getByProductAndMinRating(
            @PathVariable UUID productId,
            @PathVariable Integer minRating) {
        return reviewService.findByProductIdAndMinRating(productId, minRating);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductReview> getById(@PathVariable UUID id) {
        Optional<ProductReview> review = reviewService.findById(id);
        return review.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ProductReview create(@RequestBody ProductReview review) {
        return reviewService.save(review);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductReview> update(
            @PathVariable UUID id,
            @RequestBody ProductReview review) {
        if (!reviewService.findById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        review.setId(id);
        return ResponseEntity.ok(reviewService.save(review));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        if (!reviewService.findById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        reviewService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
