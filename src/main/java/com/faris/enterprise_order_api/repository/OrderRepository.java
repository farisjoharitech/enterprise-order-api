package com.faris.enterprise_order_api.repository;

import com.faris.enterprise_order_api.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findByStatus(String status, Pageable pageable);

    Page<Order> findByCustomer_Id(Long customerId, Pageable pageable);

    Page<Order> findByCustomer_IdAndStatus(
            Long customerId,
            String status,
            Pageable pageable
    );
}