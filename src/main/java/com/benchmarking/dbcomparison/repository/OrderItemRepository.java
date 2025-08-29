package com.benchmarking.dbcomparison.repository;

import com.benchmarking.dbcomparison.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {

    /* DELETE top-N samych pozycji (bez patrzenia na zamówienia) */

    // MySQL – klasyczny podwójny subselect
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query(value = """
        DELETE FROM order_item
        WHERE id IN (
          SELECT id FROM (
            SELECT id FROM order_item ORDER BY id LIMIT :limit
          ) t
        )
        """, nativeQuery = true)
    int deleteTopNMySql(@Param("limit") int limit);

    // Postgres – ctid + SKIP LOCKED (bez wyników SELECT)
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query(value = """
        WITH picked AS (
          SELECT ctid
          FROM order_item
          ORDER BY id
          LIMIT :limit
          FOR UPDATE SKIP LOCKED
        )
        DELETE FROM order_item oi
        USING picked p
        WHERE oi.ctid = p.ctid
        """, nativeQuery = true)
    int deleteTopNPostgres(@Param("limit") int limit);

    /* DELETE pozycji dla TOP-N zamówień (używane przed kasowaniem orders) */

    // MySQL – kasujemy po JOIN do wybranych zamówień
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query(value = """
        DELETE oi FROM order_item oi
        JOIN (
          SELECT id FROM (
            SELECT id FROM orders ORDER BY id LIMIT :limit
          ) t
        ) o ON oi.order_id = o.id
        """, nativeQuery = true)
    int deleteByTopNOrdersMySql(@Param("limit") int limit);

    // Postgres – wybieramy zamówienia (SKIP LOCKED), kasujemy ich pozycje
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query(value = """
        WITH picked AS (
          SELECT id
          FROM orders
          ORDER BY id
          LIMIT :limit
          FOR UPDATE SKIP LOCKED
        )
        DELETE FROM order_item oi
        USING picked p
        WHERE oi.order_id = p.id
        """, nativeQuery = true)
    int deleteByTopNOrdersPostgres(@Param("limit") int limit);
}
