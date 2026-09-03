package com.faris.enterprise_order_api.service;

import com.faris.enterprise_order_api.dto.CreateOrderRequest;
import com.faris.enterprise_order_api.dto.OrderResponse;
import com.faris.enterprise_order_api.dto.UpdateOrderRequest;
import com.faris.enterprise_order_api.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderServiceTest {

    private final OrderService orderService = new OrderService(new OrderRepository());

    @Test
    void createsAndFindsAnOrder() {
        OrderResponse createdOrder = orderService.createOrder(
                new CreateOrderRequest("Faris", "Keyboard", 2)
        );

        OrderResponse foundOrder = orderService.getOrderById(createdOrder.id());

        assertEquals("Faris", foundOrder.customerName());
        assertEquals("Keyboard", foundOrder.productName());
        assertEquals(2, foundOrder.quantity());
        assertEquals("CREATED", foundOrder.status());
    }

    @Test
    void updatesAnExistingOrder() {
        OrderResponse createdOrder = orderService.createOrder(
                new CreateOrderRequest("Faris", "Keyboard", 2)
        );

        OrderResponse updatedOrder = orderService.updateOrder(
                createdOrder.id(),
                new UpdateOrderRequest("Faris", "Mouse", 1, "PROCESSING")
        );

        assertEquals("Mouse", updatedOrder.productName());
        assertEquals(1, updatedOrder.quantity());
        assertEquals("PROCESSING", updatedOrder.status());
    }

    @Test
    void throwsNotFoundWhenOrderDoesNotExist() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> orderService.getOrderById(999L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void deletesAnExistingOrder() {
        OrderResponse createdOrder = orderService.createOrder(
                new CreateOrderRequest("Faris", "Keyboard", 2)
        );

        orderService.deleteOrder(createdOrder.id());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> orderService.getOrderById(createdOrder.id())
        );
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }
}
