package com.neo.customerservice.services.outbox;

public interface OutboxPublishService {

    void publishOutboxEvents();
}
