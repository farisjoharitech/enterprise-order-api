package com.faris.enterprise_order_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record UpdateOrderRequest(
        @NotBlank(message = "Customer name must not be blank")
        @Size(max = 100, message = "Customer name must not exceed 100 characters")
        String customerName,
        @NotBlank(message = "Product name must not be blank")
        @Size(max = 100, message = "Product name must not exceed 100 characters")
        String productName,
        @Positive(message = "Quantity must be greater than zero")
        int quantity,
        @NotBlank(message = "Status must not be blank")
        @Size(max = 30, message = "Status must not exceed 30 characters")
        String status
) {
}
