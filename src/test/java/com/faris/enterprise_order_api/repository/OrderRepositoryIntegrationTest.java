package com.faris.enterprise_order_api.repository;

import com.faris.enterprise_order_api.model.Customer;
import com.faris.enterprise_order_api.model.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers(disabledWithoutDocker = true)
class OrderRepositoryIntegrationTest {

    @Container
    private static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:17-alpine");

    @DynamicPropertySource
    static void configureDataSource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanDatabase() {
        orderRepository.deleteAll();
        customerRepository.deleteAll();
    }

    @Test
    void savesRetrievesUpdatesAndDeletesAnOrderInPostgreSql() {
        Customer customer = customerRepository.saveAndFlush(new Customer("Faris", "faris@example.com"));
        Order savedOrder = orderRepository.saveAndFlush(new Order(customer, "Keyboard", 2, "CREATED"));

        assertNotNull(savedOrder.getId());
        Integer persistedRows = jdbcTemplate.queryForObject(
                "select count(*) from orders where id = ?", Integer.class, savedOrder.getId()
        );
        assertEquals(1, persistedRows);

        Optional<Order> retrievedOrder = orderRepository.findById(savedOrder.getId());
        assertTrue(retrievedOrder.isPresent());
        assertEquals("Keyboard", retrievedOrder.get().getProductName());

        retrievedOrder.get().update(customer, "Mouse", 1, "PROCESSING");
        Order updatedOrder = orderRepository.saveAndFlush(retrievedOrder.get());
        assertEquals("Mouse", updatedOrder.getProductName());
        assertEquals("PROCESSING", updatedOrder.getStatus());

        orderRepository.delete(updatedOrder);
        orderRepository.flush();
        assertFalse(orderRepository.existsById(savedOrder.getId()));
    }

    @Test
    void findsOrdersByStatusWithPaginationAndSorting() {
        Customer customer = customerRepository.saveAndFlush(
                new Customer("Query Customer", "query-" + System.nanoTime() + "@example.com")
        );

        orderRepository.saveAndFlush(
                new Order(customer, "Keyboard", 2, "CREATED")
        );

        orderRepository.saveAndFlush(
                new Order(customer, "Mouse", 1, "PROCESSING")
        );

        orderRepository.saveAndFlush(
                new Order(customer, "Monitor", 1, "CREATED")
        );

        Pageable pageable = PageRequest.of(
                0,
                10,
                Sort.by(Sort.Direction.DESC, "id")
        );

        Page<Order> result = orderRepository.findByStatus(
                "CREATED",
                pageable
        );

        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());
        assertEquals("Monitor", result.getContent().get(0).getProductName());
    }

    @Test
    void findsOrdersByCustomerId() {
        Customer firstCustomer = customerRepository.saveAndFlush(
                new Customer("Customer One", "customer-one-" + System.nanoTime() + "@example.com")
        );

        Customer secondCustomer = customerRepository.saveAndFlush(
                new Customer("Customer Two", "customer-two-" + System.nanoTime() + "@example.com")
        );

        orderRepository.saveAndFlush(
                new Order(firstCustomer, "Keyboard", 2, "CREATED")
        );

        orderRepository.saveAndFlush(
                new Order(secondCustomer, "Mouse", 1, "CREATED")
        );

        Pageable pageable = PageRequest.of(0, 10);

        Page<Order> result = orderRepository.findByCustomer_Id(
                firstCustomer.getId(),
                pageable
        );

        assertEquals(1, result.getTotalElements());
        assertEquals("Keyboard", result.getContent().get(0).getProductName());
    }

    @Test
    void findsOrdersByCustomerIdAndStatus() {
        Customer customer = customerRepository.saveAndFlush(
                new Customer("Combined Customer", "combined-" + System.nanoTime() + "@example.com")
        );

        orderRepository.saveAndFlush(
                new Order(customer, "Keyboard", 2, "CREATED")
        );

        orderRepository.saveAndFlush(
                new Order(customer, "Mouse", 1, "PROCESSING")
        );

        Pageable pageable = PageRequest.of(0, 10);

        Page<Order> result =
                orderRepository.findByCustomer_IdAndStatus(
                        customer.getId(),
                        "PROCESSING",
                        pageable
                );

        assertEquals(1, result.getTotalElements());
        assertEquals("Mouse", result.getContent().get(0).getProductName());
    }

    @Test
    void paginatesOrders() {
        Customer customer = customerRepository.saveAndFlush(
                new Customer("Pagination Customer", "pagination-" + System.nanoTime() + "@example.com")
        );

        for (int i = 1; i <= 5; i++) {
            orderRepository.saveAndFlush(
                    new Order(customer, "Product " + i, i, "CREATED")
            );
        }

        Pageable pageable = PageRequest.of(
                0,
                2,
                Sort.by(Sort.Direction.ASC, "id")
        );

        Page<Order> result = orderRepository.findAll(pageable);

        assertEquals(5, result.getTotalElements());
        assertEquals(3, result.getTotalPages());
        assertEquals(2, result.getContent().size());
        assertTrue(result.isFirst());
        assertFalse(result.isLast());
    }
}
