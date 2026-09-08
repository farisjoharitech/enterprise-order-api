package com.faris.enterprise_order_api.dto;

public record OrderSummaryResponse(
        Long id,
        String productName,
        Integer quantity,
        String status,
        String customerName
) {
}