package com.benchmarking.dbcomparison.repository;

import com.benchmarking.dbcomparison.model.Product;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {
    List<Product> findTop10ByOrderByRatingDesc();
    List<Product> findTop100ByOrderByRatingDesc();

    /* --- UPDATE cen --- */
    // MySQL – wrapper subselect
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query(value = """
        UPDATE product p
        SET price = p.price * (1.0 + :percent/100.0)
        WHERE p.id IN (
          SELECT id FROM (
            SELECT id FROM product ORDER BY id LIMIT :limit
          ) t
        )
        """, nativeQuery = true)
    int bulkIncreasePriceByPercentMySql(@Param("limit") int limit, @Param("percent") double percent);

    // Postgres – USING (szybsze i czytelne)
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query(value = """
        UPDATE product p
        SET price = p.price * (1.0 + :percent/100.0)
        FROM (SELECT id FROM product ORDER BY id LIMIT :limit) s
        WHERE p.id = s.id
        """, nativeQuery = true)
    int bulkIncreasePriceByPercentPostgres(@Param("limit") int limit, @Param("percent") double percent);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query(value = """
    DELETE FROM product
    WHERE id IN (
      SELECT id FROM (
        SELECT id FROM product ORDER BY id LIMIT :limit
      ) t
    )
    """, nativeQuery = true)
    int deleteTopNMySql(@Param("limit") int limit);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query(value = """
    WITH picked AS (
      SELECT ctid
      FROM product
      ORDER BY id
      LIMIT :limit
      FOR UPDATE SKIP LOCKED
    )
    DELETE FROM product p
    USING picked s
    WHERE p.ctid = s.ctid
    """, nativeQuery = true)
    int deleteTopNPostgres(@Param("limit") int limit);
}
