package com.faris.enterprise_order_api.controller;

import com.faris.enterprise_order_api.dto.HelloResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/api/v1/hello")
    public HelloResponse hello() {
        return new HelloResponse(
                "Hello, Enterprise Java!",
                "Enterprise Order API",
                "1.0.0"
        );
    }
}
