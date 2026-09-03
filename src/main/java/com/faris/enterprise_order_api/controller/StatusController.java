package com.faris.enterprise_order_api.controller;

import com.faris.enterprise_order_api.dto.StatusResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StatusController {

    @GetMapping("/api/v1/status")
    public StatusResponse status() {
        return new StatusResponse(
                "UP",
                "Enterprise Order API",
                "1.0.0"
        );
    }
}
