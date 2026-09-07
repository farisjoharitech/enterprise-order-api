package com.faris.enterprise_order_api.dto;

public record CustomerResponse(
        Long id,
        String name,
        String email
) {
}