package com.neo.accountservice.services.consumer;


public interface CustomerEventConsumer {

    void consume(String message);
}
