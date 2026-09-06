package com.neo.customerservice.services.outbox;


import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OutboxTriggerService {
    private  final OutboxPublishService outboxPublishService;


    @Scheduled(fixedDelay = 2000)
    public void triggerOutboxPublish() {
        outboxPublishService.publishOutboxEvents();
    }

}
