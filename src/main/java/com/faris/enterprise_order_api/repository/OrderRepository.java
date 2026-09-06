package com.faris.enterprise_order_api.repository;

import com.faris.enterprise_order_api.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findByStatus(String status, Pageable pageable);

    Page<Order> findByCustomer_Id(Long customerId, Pageable pageable);

    Page<Order> findByCustomer_IdAndStatus(
            Long customerId,
            String status,
            Pageable pageable
    );

    @Query("""
        SELECT o
        FROM Order o
        JOIN FETCH o.customer
        """)
    List<Order> findAllWithCustomer();
}