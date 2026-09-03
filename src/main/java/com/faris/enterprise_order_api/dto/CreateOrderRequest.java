package com.faris.enterprise_order_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record CreateOrderRequest(
        @NotBlank String customerName,
        @NotBlank String productName,
        @Positive int quantity
) {
}
