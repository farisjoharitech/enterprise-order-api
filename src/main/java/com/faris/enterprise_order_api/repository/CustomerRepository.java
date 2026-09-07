package com.faris.enterprise_order_api.repository;

import com.faris.enterprise_order_api.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository
        extends JpaRepository<Customer, Long> {

    boolean existsByEmail(String email);
}