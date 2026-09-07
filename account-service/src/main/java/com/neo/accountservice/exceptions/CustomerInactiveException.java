package com.neo.accountservice.exceptions;

public class CustomerInactiveException extends RuntimeException {
    public CustomerInactiveException(String message) {
        super(message);
    }
}
