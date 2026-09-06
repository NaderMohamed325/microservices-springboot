package com.neo.accountservice.services.consumer;


import com.neo.accountservice.entity.Event;

public interface CustomerEventConsumer {

    public  void consume(String message);
}
