package com.faris.enterprise_order_api.controller;

import com.faris.enterprise_order_api.model.Customer;
import com.faris.enterprise_order_api.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:enterprise_order_api;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
class OrderControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    void createsAnOrder() throws Exception {
        Customer customer = customerRepository.save(new Customer("Faris", "faris-" + UUID.randomUUID() + "@example.com"));

        MvcResult result = mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderRequestJson(customer.getId(), "Keyboard", 2)))
                .andExpect(status().isCreated())
                .andExpect(header().exists(HttpHeaders.LOCATION))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.customerId").value(customer.getId().intValue()))
                .andExpect(jsonPath("$.customerName").value("Faris"))
                .andExpect(jsonPath("$.productName").value("Keyboard"))
                .andExpect(jsonPath("$.quantity").value(2))
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andReturn();

        long id = orderIdFrom(result);
        assertEquals(
                "http://localhost/api/v1/orders/" + id,
                result.getResponse().getHeader(HttpHeaders.LOCATION)
        );
    }

    @Test
    void returnsValidationErrorForNullCustomerId() throws Exception {
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productName\":\"Keyboard\",\"quantity\":2}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.path").value("/api/v1/orders"))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("customerId"))
                .andExpect(jsonPath("$.fieldErrors[0].message").value("Customer ID must not be null"));
    }

    @Test
    void returnsValidationErrorForInvalidCustomerId() throws Exception {
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderRequestJson(0L, "Keyboard", 2)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("customerId"))
                .andExpect(jsonPath("$.fieldErrors[0].message").value("Customer ID must be positive"));
    }

    @Test
    void returnsValidationErrorForBlankProductName() throws Exception {
        Customer customer = customerRepository.save(new Customer("Faris", "faris-blank-" + UUID.randomUUID() + "@example.com"));

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderRequestJson(customer.getId(), " ", 2)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("productName"))
                .andExpect(jsonPath("$.fieldErrors[0].message").value("Product name must not be blank"));
    }

    @Test
    void returnsValidationErrorForInvalidQuantity() throws Exception {
        Customer customer = customerRepository.save(new Customer("Faris", "faris-qty-" + UUID.randomUUID() + "@example.com"));

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderRequestJson(customer.getId(), "Keyboard", 0)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("quantity"))
                .andExpect(jsonPath("$.fieldErrors[0].message").value("Quantity must be greater than zero"));
    }

    @Test
    void returnsNotFoundForNonExistentCustomer() throws Exception {
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderRequestJson(999L, "Keyboard", 2)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Customer 999 was not found"));
    }

    @Test
    void listsOrders() throws Exception {
        Customer customer = customerRepository.save(new Customer("List Customer", "list-" + UUID.randomUUID() + "@example.com"));
        createOrder(customer.getId(), "List Product", 3);

        mockMvc.perform(get("/api/v1/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.customerName == 'List Customer')]").isNotEmpty());
    }

    @Test
    void getsAnOrderById() throws Exception {
        Customer customer = customerRepository.save(new Customer("Get Customer", "get-" + UUID.randomUUID() + "@example.com"));
        long id = createOrder(customer.getId(), "Get Product", 1);

        mockMvc.perform(get("/api/v1/orders/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.productName").value("Get Product"));
    }

    @Test
    void updatesAnOrder() throws Exception {
        Customer customer = customerRepository.save(new Customer("Update Customer", "update-" + UUID.randomUUID() + "@example.com"));
        long id = createOrder(customer.getId(), "Original Product", 1);

        mockMvc.perform(put("/api/v1/orders/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateOrderRequestJson(
                                customer.getId(), "Updated Product", 5, "PROCESSING"
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.productName").value("Updated Product"))
                .andExpect(jsonPath("$.quantity").value(5))
                .andExpect(jsonPath("$.status").value("PROCESSING"));
    }

    @Test
    void returnsValidationErrorWhenUpdateStatusIsMissing() throws Exception {
        Customer customer = customerRepository.save(new Customer("Faris", "faris-status-" + UUID.randomUUID() + "@example.com"));
        long id = createOrder(customer.getId(), "Keyboard", 1);

        mockMvc.perform(put("/api/v1/orders/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderRequestJson(customer.getId(), "Laptop", 5)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.path").value("/api/v1/orders/" + id))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("status"))
                .andExpect(jsonPath("$.fieldErrors[0].message").value("Status must not be blank"));
    }

    @Test
    void deletesAnOrder() throws Exception {
        Customer customer = customerRepository.save(new Customer("Delete Customer", "delete-" + UUID.randomUUID() + "@example.com"));
        long id = createOrder(customer.getId(), "Delete Product", 1);

        mockMvc.perform(delete("/api/v1/orders/{id}", id))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        mockMvc.perform(get("/api/v1/orders/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void returnsNotFoundForUnknownOrder() throws Exception {
        mockMvc.perform(get("/api/v1/orders/{id}", 999999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Order 999999 was not found"))
                .andExpect(jsonPath("$.path").value("/api/v1/orders/999999"))
                .andExpect(jsonPath("$.fieldErrors").isEmpty());
    }

    @Test
    void getExistingOrderReturns200() throws Exception {
        Customer customer = customerRepository.save(new Customer("Test Customer", "get-existing-" + UUID.randomUUID() + "@example.com"));
        long id = createOrder(customer.getId(), "Test Product", 5);

        mockMvc.perform(get("/api/v1/orders/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.customerId").value(customer.getId().intValue()))
                .andExpect(jsonPath("$.customerName").value("Test Customer"))
                .andExpect(jsonPath("$.productName").value("Test Product"))
                .andExpect(jsonPath("$.quantity").value(5));
    }

    @Test
    void getMissingOrderReturns404WithErrorStructure() throws Exception {
        mockMvc.perform(get("/api/v1/orders/{id}", 888888L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Order 888888 was not found"))
                .andExpect(jsonPath("$.fieldErrors").isEmpty());
    }

    @Test
    void postInvalidRequestReturns400WithValidationError() throws Exception {
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productName\":\"Product\",\"quantity\":1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("customerId"))
                .andExpect(jsonPath("$.fieldErrors[0].message").value("Customer ID must not be null"));
    }

    private long createOrder(Long customerId, String productName, int quantity) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderRequestJson(customerId, productName, quantity)))
                .andExpect(status().isCreated())
                .andExpect(header().exists(HttpHeaders.LOCATION))
                .andReturn();

        return orderIdFrom(result);
    }

    private long orderIdFrom(MvcResult result) throws Exception {
        String responseBody = result.getResponse().getContentAsString();
        Matcher matcher = Pattern.compile("\\\"id\\\":(\\d+)").matcher(responseBody);
        if (!matcher.find()) {
            throw new IllegalStateException("Created order response did not include an id");
        }
        return Long.parseLong(matcher.group(1));
    }

    private String orderRequestJson(Long customerId, String productName, int quantity) {
        return "{\"customerId\":%d,\"productName\":\"%s\",\"quantity\":%d}"
                .formatted(customerId, productName, quantity);
    }

    private String updateOrderRequestJson(Long customerId, String productName, int quantity, String status) {
        return "{\"customerId\":%d,\"productName\":\"%s\",\"quantity\":%d,\"status\":\"%s\"}"
                .formatted(customerId, productName, quantity, status);
    }
}
