package com.faris.enterprise_order_api.dto;

public record StatusResponse(
        String status,
        String application,
        String version
) {
}
