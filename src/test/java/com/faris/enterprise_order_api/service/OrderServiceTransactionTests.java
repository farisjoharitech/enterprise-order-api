package com.faris.enterprise_order_api.service;

import com.faris.enterprise_order_api.model.Customer;
import com.faris.enterprise_order_api.model.Order;
import com.faris.enterprise_order_api.repository.CustomerRepository;
import com.faris.enterprise_order_api.repository.OrderRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.OptimisticLockException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class OrderServiceTransactionTests {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldPersistOrderWithInitialVersion() {
        Customer customer =
                customerRepository.save(
                        new Customer("Version Test Customer", "version-test-" + UUID.randomUUID() + "@example.com")
                );

        Order order =
                orderRepository.save(
                        new Order(customer, "Version Test Product", 1, "NEW")
                );

        assertNotNull(order.getId());
        assertNotNull(order.getVersion());
        assertEquals(0L, order.getVersion());
    }

    @Test
    void shouldPreventConcurrentUpdatesWithOptimisticLocking()
            throws Exception {

        TransactionTemplate transactionTemplate =
                new TransactionTemplate(transactionManager);

        Long orderId = transactionTemplate.execute(status -> {

            Customer customer =
                    customerRepository.save(
                            new Customer(
                                    "Concurrency Customer",
                                    "concurrency-" + UUID.randomUUID() + "@example.com"
                            )
                    );

            Order order =
                    orderRepository.save(
                            new Order(
                                    customer,
                                    "Original Product",
                                    1,
                                    "NEW"
                            )
                    );

            entityManager.flush();

            return order.getId();
        });

        CountDownLatch firstTransactionReady =
                new CountDownLatch(1);

        CountDownLatch secondTransactionReady =
                new CountDownLatch(1);

        AtomicReference<Exception> secondTransactionException =
                new AtomicReference<>();

        Thread firstTransaction = new Thread(() -> {

            transactionTemplate.executeWithoutResult(status -> {

                Order order =
                        orderRepository.findById(orderId)
                                .orElseThrow();

                assertEquals(0L, order.getVersion());

                order.update(
                        order.getCustomer(),
                        "Product Updated By Transaction A",
                        1,
                        "PROCESSING"
                );

                entityManager.flush();

                firstTransactionReady.countDown();

                try {
                    secondTransactionReady.await(
                            5,
                            TimeUnit.SECONDS
                    );
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException(e);
                }
            });
        });

        Thread secondTransaction = new Thread(() -> {

            try {

                transactionTemplate.executeWithoutResult(status -> {

                    Order order =
                            orderRepository.findById(orderId)
                                    .orElseThrow();

                    assertEquals(0L, order.getVersion());

                    secondTransactionReady.countDown();

                    try {
                        firstTransactionReady.await(
                                5,
                                TimeUnit.SECONDS
                        );
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new IllegalStateException(e);
                    }

                    order.update(
                            order.getCustomer(),
                            "Product Updated By Transaction B",
                            1,
                            "CANCELLED"
                    );

                    entityManager.flush();
                });

            } catch (Exception e) {
                secondTransactionException.set(e);
            }
        });

        firstTransaction.start();
        secondTransaction.start();

        firstTransaction.join();
        secondTransaction.join();

        Exception exception =
                secondTransactionException.get();

        assertNotNull(exception);

        assertTrue(
                containsOptimisticLockException(exception)
        );
    }

    private boolean containsOptimisticLockException(Throwable throwable) {

        Throwable current = throwable;

        while (current != null) {

            if (current instanceof OptimisticLockException) {
                return true;
            }

            current = current.getCause();
        }

        return false;
    }

    @Test
    void shouldRollbackTransactionWhenRuntimeExceptionOccurs() {

        TransactionTemplate transactionTemplate =
                new TransactionTemplate(transactionManager);

        String email =
                "rollback-" + UUID.randomUUID() + "@example.com";

        assertThrows(
                IllegalStateException.class,
                () -> transactionTemplate.executeWithoutResult(status -> {

                    Customer customer =
                            customerRepository.save(
                                    new Customer(
                                            "Rollback Customer",
                                            email
                                    )
                            );

                    entityManager.flush();

                    throw new IllegalStateException(
                            "Simulated business failure"
                    );
                })
        );

        assertTrue(
                customerRepository.findAll()
                        .stream()
                        .noneMatch(customer -> email.equals(customer.getEmail()))
        );
    }
}