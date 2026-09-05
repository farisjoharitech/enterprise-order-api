package com.faris.enterprise_order_api.service;

import com.faris.enterprise_order_api.dto.CreateOrderRequest;
import com.faris.enterprise_order_api.dto.OrderResponse;
import com.faris.enterprise_order_api.dto.UpdateOrderRequest;
import com.faris.enterprise_order_api.model.Customer;
import com.faris.enterprise_order_api.model.Order;
import com.faris.enterprise_order_api.repository.CustomerRepository;
import com.faris.enterprise_order_api.repository.OrderRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;

    public OrderService(OrderRepository orderRepository, CustomerRepository customerRepository) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
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
        Customer customer = findCustomer(request.customerId());
        Order order = new Order(
                customer,
                request.productName(),
                request.quantity(),
                "CREATED"
        );
        return toResponse(orderRepository.save(order));
    }

    public OrderResponse updateOrder(Long id, UpdateOrderRequest request) {
        Order order = findOrder(id);
        Customer customer = findCustomer(request.customerId());
        order.update(
                customer,
                request.productName(),
                request.quantity(),
                request.status()
        );
        return toResponse(orderRepository.save(order));
    }

    public void deleteOrder(Long id) {
        Order order = findOrder(id);
        orderRepository.delete(order);
    }

    private Order findOrder(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> orderNotFound(id));
    }

    private Customer findCustomer(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> customerNotFound(id));
    }

    private ResponseStatusException orderNotFound(Long id) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Order " + id + " was not found");
    }

    private ResponseStatusException customerNotFound(Long id) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer " + id + " was not found");
    }

    private OrderResponse toResponse(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getCustomer().getId(),
                order.getCustomer().getName(),
                order.getProductName(),
                order.getQuantity(),
                order.getStatus()
        );
    }
}
