package com.faris.enterprise_order_api.controller;

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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createsAnOrder() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderRequestJson("Faris", "Keyboard", 2)))
                .andExpect(status().isCreated())
                .andExpect(header().exists(HttpHeaders.LOCATION))
                .andExpect(jsonPath("$.id").isNumber())
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
    void returnsValidationErrorForBlankCustomerName() throws Exception {
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderRequestJson(" ", "Keyboard", 2)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.path").value("/api/v1/orders"))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("customerName"))
                .andExpect(jsonPath("$.fieldErrors[0].message").value("Customer name must not be blank"));
    }

    @Test
    void returnsValidationErrorForBlankProductName() throws Exception {
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderRequestJson("Faris", " ", 2)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("productName"))
                .andExpect(jsonPath("$.fieldErrors[0].message").value("Product name must not be blank"));
    }

    @Test
    void returnsValidationErrorForInvalidQuantity() throws Exception {
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderRequestJson("Faris", "Keyboard", 0)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("quantity"))
                .andExpect(jsonPath("$.fieldErrors[0].message").value("Quantity must be greater than zero"));
    }

    @Test
    void listsOrders() throws Exception {
        createOrder("List Customer", "List Product", 3);

        mockMvc.perform(get("/api/v1/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.customerName == 'List Customer')]").isNotEmpty());
    }

    @Test
    void getsAnOrderById() throws Exception {
        long id = createOrder("Get Customer", "Get Product", 1);

        mockMvc.perform(get("/api/v1/orders/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.productName").value("Get Product"));
    }

    @Test
    void updatesAnOrder() throws Exception {
        long id = createOrder("Update Customer", "Original Product", 1);

        mockMvc.perform(put("/api/v1/orders/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateOrderRequestJson(
                                "Update Customer", "Updated Product", 5, "PROCESSING"
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.productName").value("Updated Product"))
                .andExpect(jsonPath("$.quantity").value(5))
                .andExpect(jsonPath("$.status").value("PROCESSING"));
    }

    @Test
    void returnsValidationErrorWhenUpdateStatusIsMissing() throws Exception {
        long id = createOrder("Faris", "Keyboard", 1);

        mockMvc.perform(put("/api/v1/orders/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderRequestJson("Faris", "Laptop", 5)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.path").value("/api/v1/orders/" + id))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("status"))
                .andExpect(jsonPath("$.fieldErrors[0].message").value("Status must not be blank"));
    }

    @Test
    void deletesAnOrder() throws Exception {
        long id = createOrder("Delete Customer", "Delete Product", 1);

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

    private long createOrder(String customerName, String productName, int quantity) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderRequestJson(customerName, productName, quantity)))
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

    private String orderRequestJson(String customerName, String productName, int quantity) {
        return "{\"customerName\":\"%s\",\"productName\":\"%s\",\"quantity\":%d}"
                .formatted(customerName, productName, quantity);
    }

    private String updateOrderRequestJson(String customerName, String productName, int quantity, String status) {
        return "{\"customerName\":\"%s\",\"productName\":\"%s\",\"quantity\":%d,\"status\":\"%s\"}"
                .formatted(customerName, productName, quantity, status);
    }
}
