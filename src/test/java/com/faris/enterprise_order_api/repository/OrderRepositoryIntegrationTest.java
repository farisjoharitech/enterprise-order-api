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
}
