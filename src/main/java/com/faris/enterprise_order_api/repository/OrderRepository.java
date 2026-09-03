package com.faris.enterprise_order_api.repository;

import com.faris.enterprise_order_api.model.Order;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class OrderRepository {

    private final AtomicLong nextId = new AtomicLong(1);
    private final ConcurrentHashMap<Long, Order> orders = new ConcurrentHashMap<>();

    public List<Order> findAll() {
        return orders.values().stream()
                .sorted(Comparator.comparing(Order::id))
                .toList();
    }

    public Optional<Order> findById(Long id) {
        return Optional.ofNullable(orders.get(id));
    }

    public Order create(String customerName, String productName, int quantity) {
        Long id = nextId.getAndIncrement();
        Order order = new Order(id, customerName, productName, quantity, "CREATED");
        orders.put(id, order);
        return order;
    }

    public Order save(Order order) {
        orders.put(order.id(), order);
        return order;
    }

    public boolean deleteById(Long id) {
        return orders.remove(id) != null;
    }
}
