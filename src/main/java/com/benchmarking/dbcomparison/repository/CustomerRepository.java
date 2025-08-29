package com.benchmarking.dbcomparison.repository;

import com.benchmarking.dbcomparison.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {
    Optional<Customer> findByEmail(String email);

    List<Customer> findByStatus(String status);

    List<Customer> findByNewsletterSubscription(Boolean subscribed);

    boolean existsByEmail(String email);

    /* --- UPDATE emaili --- */
    // Postgres: CAST(UUID as text)
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            UPDATE customer c
            SET email = CONCAT(:prefix, '+', CAST(c.id AS text), '@mail.com')
            WHERE c.id IN (SELECT id FROM customer ORDER BY id LIMIT :limit)
            """, nativeQuery = true)
    int bulkUpdateEmailsPostgres(@Param("limit") int limit, @Param("prefix") String prefix);

    // MySQL: BINARY(16) -> string
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            UPDATE customer c
            SET email = CONCAT(:prefix, '+', BIN_TO_UUID(c.id), '@mail.com')
            WHERE c.id IN (
              SELECT id FROM (
                SELECT id FROM customer ORDER BY id LIMIT :limit
              ) t
            )
            """, nativeQuery = true)
    int bulkUpdateEmailsMySql(@Param("limit") int limit, @Param("prefix") String prefix);

    /* --- DELETE top N --- */
    // DELETE top-N MySQL
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query(value = """
            DELETE FROM customer
            WHERE id IN (
              SELECT id FROM (
                SELECT id FROM customer ORDER BY id LIMIT :limit
              ) t
            )
            """, nativeQuery = true)
    int deleteTopNMySql(@Param("limit") int limit);

    // DELETE top-N Postgres (bezpieczny wariant ctid + SKIP LOCKED)
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query(value = """
            WITH picked AS (
              SELECT ctid
              FROM customer
              ORDER BY id
              LIMIT :limit
              FOR UPDATE SKIP LOCKED
            )
            DELETE FROM customer c
            USING picked p
            WHERE c.ctid = p.ctid
            """, nativeQuery = true)
    int deleteTopNPostgres(@Param("limit") int limit);
}

