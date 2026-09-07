package com.neo.accountservice.exceptions;

public class SalaryAccountAlreadyExistsException extends RuntimeException {
    public SalaryAccountAlreadyExistsException(String message) {
        super(message);
    }
}
