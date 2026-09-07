package com.neo.accountservice.exceptions;

public class MaxAccountsReachedException extends RuntimeException {
    public MaxAccountsReachedException(String message) {
        super(message);
    }
}
