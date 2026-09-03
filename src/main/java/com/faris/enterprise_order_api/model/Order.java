package com.faris.enterprise_order_api.model;

public record Order(
        Long id,
        String customerName,
        String productName,
        int quantity,
        String status
) {
}
