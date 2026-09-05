package com.neo.customerservice.services.producer;

public interface ProducerService {
    <T> void sendMessage(String topic, T message);
}
