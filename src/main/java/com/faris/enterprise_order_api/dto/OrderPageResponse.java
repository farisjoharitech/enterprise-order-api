package com.faris.enterprise_order_api.dto;

import java.util.List;

public record OrderPageResponse(
        List<OrderResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {
}
