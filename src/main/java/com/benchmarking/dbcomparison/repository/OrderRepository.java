package com.benchmarking.dbcomparison.repository;

import com.benchmarking.dbcomparison.model.Order;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    /* ======================= READ (do benchmarku) ======================= */

    // Stronicowany odczyt samych ID – działa na obu DB (JPQL, bez natywki)
    @Query("select o.id from Order o order by o.id")
    List<UUID> findOrderIdsPage(Pageable pageable);

    // Dociągnięcie zamówień z klientem i pozycjami (i produktem) – batch by ids
    @Query("""
           select distinct o
           from Order o
           left join fetch o.customer c
           left join fetch o.items i
           left join fetch i.product p
           where o.id in :ids
           """)
    List<Order> findAllWithCustomerAndItemsByIdIn(@Param("ids") List<UUID> ids);

    /* ======================= UPDATE statusu ======================= */

    // Postgres: UPDATE ... FROM (SELECT ... LIMIT)
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query(value = """
        UPDATE orders o
        SET status = :status
        FROM (SELECT id FROM orders ORDER BY id LIMIT :limit) s
        WHERE o.id = s.id
        """, nativeQuery = true)
    int bulkUpdateStatusPostgres(@Param("limit") int limit, @Param("status") String status);

    // MySQL: UPDATE z JOIN na podzapytaniu z LIMIT
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query(value = """
        UPDATE orders o
        JOIN (SELECT id FROM orders ORDER BY id LIMIT :limit) t
          ON t.id = o.id
        SET o.status = :status
        """, nativeQuery = true)
    int bulkUpdateStatusMySql(@Param("limit") int limit, @Param("status") String status);

    /* ======================= DELETE top-N ======================= */

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query(value = """
        DELETE FROM orders
        WHERE id IN (
          SELECT id FROM (
            SELECT id FROM orders ORDER BY id LIMIT :limit
          ) t
        )
        """, nativeQuery = true)
    int deleteTopNMySql(@Param("limit") int limit);

    // Postgres – JEDEN statement: pobierz TOP-N zamówień, usuń ich pozycje, usuń same zamówienia.
    // Brak SELECT na końcu -> nie będzie błędu „zwrócono wynik zapytania”.
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query(value = """
        WITH picked AS (
          SELECT id, ctid
          FROM orders
          ORDER BY id
          LIMIT :limit
          FOR UPDATE SKIP LOCKED
        ), del_items AS (
          DELETE FROM order_item oi
          USING picked p
          WHERE oi.order_id = p.id
        )
        DELETE FROM orders o
        USING picked p
        WHERE o.ctid = p.ctid
        """, nativeQuery = true)
    int deleteTopNPostgres(@Param("limit") int limit);
}
