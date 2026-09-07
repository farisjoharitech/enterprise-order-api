package com.faris.enterprise_order_api.exception;

public class CustomerNotFoundException extends RuntimeException {

    public CustomerNotFoundException(Long id) {
        super("Customer " + id + " was not found");
    }
}