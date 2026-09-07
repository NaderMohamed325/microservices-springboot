package com.neo.accountservice.services.consumer;


import com.neo.accountservice.dto.user.events.UserCreatedEvent;
import com.neo.accountservice.dto.user.events.UserDeletedEvent;
import com.neo.accountservice.dto.user.events.UserStatusChangedEvent;
import com.neo.accountservice.entity.Event;
import com.neo.accountservice.enums.AccountType;
import com.neo.accountservice.services.account.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerEventConsumerImpl implements CustomerEventConsumer {


    private final ObjectMapper objectMapper;
    private final AccountService accountService;

    @Override
    @KafkaListener(
            topics = "USER_CUSTOMER",
            groupId = "accounts-service-group"
    )
    public void consume(String message) {
        Event event = objectMapper.readValue(message, Event.class);
        log.info("Received event: {}", event);
        switch (event.getEventType()) {
            case USER_CUSTOMER_CREATED:
                handleCustomerCreatedEvent(event);
                break;

            case USER_CUSTOMER_DELETED:
                handleCustomerDeletedEvent(event);
                break;

            case USER_CUSTOMER_STATUS_CHANGED:
                handleCustomerStatusChangedEvent(event);
                break;

            default:
                log.warn("Received unknown event type: {}", event.getEventType());
        }

    }


    void handleCustomerCreatedEvent(Event event) {
        UserCreatedEvent userCreatedEvent = objectMapper.readValue(event.getPayload(), UserCreatedEvent.class);
        log.info("Received UserCreatedEvent: {}", userCreatedEvent);
        accountService.createAccount(userCreatedEvent.getUserId(), AccountType.SAVINGS);
    }


    void handleCustomerDeletedEvent(Event event) {
        UserDeletedEvent userDeletedEvent = objectMapper.readValue(event.getPayload(), UserDeletedEvent.class);
        log.info("Received UserDeletedEvent: {}", userDeletedEvent);
        accountService.deleteAccountsByCustomerId(userDeletedEvent.getUserId());
    }

    void handleCustomerStatusChangedEvent(Event event) {
        UserStatusChangedEvent statusChangedEvent = objectMapper.readValue(event.getPayload(), UserStatusChangedEvent.class);
        log.info("Received UserStatusChangedEvent: {}", statusChangedEvent);
        accountService.updateCustomerStatus(statusChangedEvent.getUserId(), statusChangedEvent.isEnabled());
    }
}
