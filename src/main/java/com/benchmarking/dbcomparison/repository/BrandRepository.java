package com.benchmarking.dbcomparison.repository;

import com.benchmarking.dbcomparison.model.Brand;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Repository
public interface BrandRepository extends JpaRepository<Brand, UUID> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query(value = """
    DELETE FROM brand
    WHERE id IN (
      SELECT id FROM (
        SELECT id FROM brand ORDER BY id LIMIT :limit
      ) t
    )
    """, nativeQuery = true)
    int deleteTopNMySql(@Param("limit") int limit);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query(value = """
    WITH picked AS (
      SELECT ctid
      FROM brand
      ORDER BY id
      LIMIT :limit
      FOR UPDATE SKIP LOCKED
    )
    DELETE FROM brand b
    USING picked p
    WHERE b.ctid = p.ctid
    """, nativeQuery = true)
    int deleteTopNPostgres(@Param("limit") int limit);

}

