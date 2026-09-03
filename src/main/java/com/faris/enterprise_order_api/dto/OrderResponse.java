package com.faris.enterprise_order_api.dto;

public record OrderResponse(
        Long id,
        String customerName,
        String productName,
        int quantity,
        String status
) {
}
