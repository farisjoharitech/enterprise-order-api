package com.faris.enterprise_order_api.service;

import com.faris.enterprise_order_api.dto.CreateOrderRequest;
import com.faris.enterprise_order_api.dto.OrderResponse;
import com.faris.enterprise_order_api.dto.UpdateOrderRequest;
import com.faris.enterprise_order_api.model.Order;
import com.faris.enterprise_order_api.repository.OrderRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public OrderResponse getOrderById(Long id) {
        return toResponse(findOrder(id));
    }

    public OrderResponse createOrder(CreateOrderRequest request) {
        Order order = orderRepository.create(
                request.customerName(),
                request.productName(),
                request.quantity()
        );
        return toResponse(order);
    }

    public OrderResponse updateOrder(Long id, UpdateOrderRequest request) {
        findOrder(id);
        Order updatedOrder = new Order(
                id,
                request.customerName(),
                request.productName(),
                request.quantity(),
                request.status()
        );
        return toResponse(orderRepository.save(updatedOrder));
    }

    public void deleteOrder(Long id) {
        if (!orderRepository.deleteById(id)) {
            throw orderNotFound(id);
        }
    }

    private Order findOrder(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> orderNotFound(id));
    }

    private ResponseStatusException orderNotFound(Long id) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Order " + id + " was not found");
    }

    private OrderResponse toResponse(Order order) {
        return new OrderResponse(
                order.id(),
                order.customerName(),
                order.productName(),
                order.quantity(),
                order.status()
        );
    }
}
