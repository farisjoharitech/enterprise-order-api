package com.faris.enterprise_order_api.service;

import com.faris.enterprise_order_api.dto.CreateOrderRequest;
import com.faris.enterprise_order_api.dto.OrderResponse;
import com.faris.enterprise_order_api.dto.UpdateOrderRequest;
import com.faris.enterprise_order_api.model.Order;
import com.faris.enterprise_order_api.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(orderRepository);
    }

    @Test
    void createsAnOrderWithCreatedStatus() {
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponse createdOrder = orderService.createOrder(
                new CreateOrderRequest("Faris", "Keyboard", 2)
        );

        assertEquals("Faris", createdOrder.customerName());
        assertEquals("Keyboard", createdOrder.productName());
        assertEquals(2, createdOrder.quantity());
        assertEquals("CREATED", createdOrder.status());
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void updatesAnExistingOrder() {
        Order order = new Order("Faris", "Keyboard", 2, "CREATED");
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponse updatedOrder = orderService.updateOrder(
                1L,
                new UpdateOrderRequest("Faris", "Mouse", 1, "PROCESSING")
        );

        assertEquals("Mouse", updatedOrder.productName());
        assertEquals(1, updatedOrder.quantity());
        assertEquals("PROCESSING", updatedOrder.status());
    }

    @Test
    void throwsNotFoundWhenOrderDoesNotExist() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> orderService.getOrderById(999L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void deletesAnExistingOrder() {
        Order order = new Order("Faris", "Keyboard", 2, "CREATED");
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        orderService.deleteOrder(1L);

        verify(orderRepository).delete(order);
    }
}
