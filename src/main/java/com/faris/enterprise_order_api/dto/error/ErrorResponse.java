package com.faris.enterprise_order_api.dto.error;

import java.util.List;

public record ErrorResponse(
        int status,
        String error,
        String message,
        String path,
        List<FieldErrorResponse> fieldErrors
) {
}
