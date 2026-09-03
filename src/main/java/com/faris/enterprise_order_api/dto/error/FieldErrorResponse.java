package com.faris.enterprise_order_api.dto.error;

public record FieldErrorResponse(
        String field,
        String message
) {
}
