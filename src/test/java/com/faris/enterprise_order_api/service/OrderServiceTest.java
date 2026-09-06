package com.faris.enterprise_order_api.service;

import com.faris.enterprise_order_api.dto.CreateOrderRequest;
import com.faris.enterprise_order_api.dto.OrderResponse;
import com.faris.enterprise_order_api.dto.UpdateOrderRequest;
import com.faris.enterprise_order_api.exception.OrderNotFoundException;
import com.faris.enterprise_order_api.model.Customer;
import com.faris.enterprise_order_api.model.Order;
import com.faris.enterprise_order_api.repository.CustomerRepository;
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

    @Mock
    private CustomerRepository customerRepository;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(orderRepository, customerRepository);
    }

    @Test
    void createsAnOrderWithCreatedStatus() {
        Customer customer = new Customer("Faris", "faris@example.com");
        customer.setId(1L);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponse createdOrder = orderService.createOrder(
                new CreateOrderRequest(1L, "Keyboard", 2)
        );

        assertEquals(1L, createdOrder.customerId());
        assertEquals("Faris", createdOrder.customerName());
        assertEquals("Keyboard", createdOrder.productName());
        assertEquals(2, createdOrder.quantity());
        assertEquals("CREATED", createdOrder.status());
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void updatesAnExistingOrder() {
        Customer customer = new Customer("Faris", "faris@example.com");
        customer.setId(1L);
        Order order = new Order(customer, "Keyboard", 2, "CREATED");
        order.setId(1L);
        
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponse updatedOrder = orderService.updateOrder(
                1L,
                new UpdateOrderRequest(1L, "Mouse", 1, "PROCESSING")
        );

        assertEquals("Mouse", updatedOrder.productName());
        assertEquals(1, updatedOrder.quantity());
        assertEquals("PROCESSING", updatedOrder.status());
    }

    @Test
    void throwsNotFoundWhenOrderDoesNotExist() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        OrderNotFoundException exception = assertThrows(
                OrderNotFoundException.class,
                () -> orderService.getOrderById(999L)
        );

        assertEquals("Order 999 was not found", exception.getMessage());
    }

    @Test
    void throwsNotFoundWhenCustomerDoesNotExist() {
        when(customerRepository.findById(999L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> orderService.createOrder(new CreateOrderRequest(999L, "Keyboard", 2))
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void deletesAnExistingOrder() {
        Customer customer = new Customer("Faris", "faris@example.com");
        customer.setId(1L);
        Order order = new Order(customer, "Keyboard", 2, "CREATED");
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        orderService.deleteOrder(1L);

        verify(orderRepository).delete(order);
    }
}
