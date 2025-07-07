package com.benchmarking.dbcomparison.repository;

import com.benchmarking.dbcomparison.model.ProductReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface ProductReviewRepository extends JpaRepository<ProductReview, UUID> {
    List<ProductReview> findByProductId(UUID productId);
    List<ProductReview> findByRating(Integer rating);
    List<ProductReview> findByCustomerId(UUID customerId);
    List<ProductReview> findByIsVerified(Boolean isVerified);
    List<ProductReview> findByProductIdAndRatingGreaterThanEqual(UUID productId, Integer minRating);

    @Query("SELECT pr FROM ProductReview pr WHERE pr.product.id = ?1 ORDER BY pr.helpfulVotes DESC")
    List<ProductReview> findMostHelpfulReviews(UUID productId);
}
