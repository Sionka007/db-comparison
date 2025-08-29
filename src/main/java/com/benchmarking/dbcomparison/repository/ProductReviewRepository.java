package com.benchmarking.dbcomparison.repository;

import com.benchmarking.dbcomparison.model.ProductReview;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Repository
public interface ProductReviewRepository extends JpaRepository<ProductReview, UUID> {
    List<ProductReview> findByProductId(UUID productId);
    List<ProductReview> findByRating(Integer rating);
    List<ProductReview> findByCustomerId(UUID customerId);
    List<ProductReview> findByIsVerified(Boolean isVerified);
    List<ProductReview> findByProductIdAndRatingGreaterThanEqual(UUID productId, Integer minRating);

    @Query("SELECT pr FROM ProductReview pr WHERE pr.product.id = ?1 ORDER BY pr.helpfulVotes DESC")
    List<ProductReview> findMostHelpfulReviews(UUID productId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query(value = """
    DELETE FROM product_review
    WHERE id IN (
      SELECT id FROM (
        SELECT id FROM product_review ORDER BY id LIMIT :limit
      ) t
    )
    """, nativeQuery = true)
    int deleteTopNMySql(@Param("limit") int limit);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query(value = """
    WITH picked AS (
      SELECT ctid
      FROM product_review
      ORDER BY id
      LIMIT :limit
      FOR UPDATE SKIP LOCKED
    )
    DELETE FROM product_review pr
    USING picked p
    WHERE pr.ctid = p.ctid
    """, nativeQuery = true)
    int deleteTopNPostgres(@Param("limit") int limit);

}