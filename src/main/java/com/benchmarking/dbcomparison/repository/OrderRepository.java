package com.benchmarking.dbcomparison.repository;

import com.benchmarking.dbcomparison.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.customer LEFT JOIN FETCH o.items i LEFT JOIN FETCH i.product")
    List<Order> findAllWithCustomerAndItems();

    @Query("SELECT pc.name as category, COUNT(oi) as count, SUM(oi.totalAmount) as total " +
           "FROM Order o " +
           "JOIN o.items oi " +
           "JOIN oi.product p " +
           "JOIN p.category pc " +
           "GROUP BY pc.name " +
           "ORDER BY total DESC")
    List<Object[]> findSalesByCategory();
}
