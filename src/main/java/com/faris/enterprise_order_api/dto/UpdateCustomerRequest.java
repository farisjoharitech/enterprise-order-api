package com.faris.enterprise_order_api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateCustomerRequest(

        @NotBlank(message = "Customer name must not be blank")
        @Size(max = 100, message = "Customer name must not exceed 100 characters")
        String name,

        @NotBlank(message = "Customer email must not be blank")
        @Email(message = "Customer email must be valid")
        @Size(max = 255, message = "Customer email must not exceed 255 characters")
        String email
) {
}