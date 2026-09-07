package com.neo.accountservice.services.consumer;

import com.neo.accountservice.dto.user.events.UserCreatedEvent;
import com.neo.accountservice.entity.Event;
import com.neo.accountservice.enums.AccountType;
import com.neo.accountservice.enums.EventType;
import com.neo.accountservice.services.account.AccountService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerEventConsumerImplTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private AccountService accountService;

    @InjectMocks
    private CustomerEventConsumerImpl customerEventConsumer;

    @Test
    void consume_userCreatedEvent_callsCreateAccount() throws Exception {
        String message = "{}";
        Event event = Event.builder()
                .eventType(EventType.USER_CUSTOMER_CREATED)
                .payload("{}")
                .build();
        UserCreatedEvent userCreatedEvent = UserCreatedEvent.builder()
                .userId(1L)
                .username("testuser")
                .email("test@example.com")
                .build();

        when(objectMapper.readValue(eq(message), eq(Event.class))).thenReturn(event);
        when(objectMapper.readValue(eq("{}"), eq(UserCreatedEvent.class))).thenReturn(userCreatedEvent);

        customerEventConsumer.consume(message);

        verify(accountService).createAccount(1L, AccountType.SAVINGS);
    }

    @Test
    void consume_unknownEventType_doesNothing() throws Exception {
        String message = "{}";
        Event event = Event.builder()
                .eventType(EventType.USER_CUSTOMER_CREATED)
                .payload("{}")
                .build();

        when(objectMapper.readValue(eq(message), eq(Event.class))).thenReturn(event);
        when(objectMapper.readValue(eq("{}"), eq(UserCreatedEvent.class))).thenReturn(
                UserCreatedEvent.builder().userId(1L).username("test").email("test@test.com").build()
        );

        customerEventConsumer.consume(message);

        verify(accountService).createAccount(1L, AccountType.SAVINGS);
    }

    @Test
    void handleCustomerCreatedEvent_createsAccountForUser() throws Exception {
        Event event = Event.builder()
                .eventType(EventType.USER_CUSTOMER_CREATED)
                .payload("{\"userId\":1,\"username\":\"testuser\",\"email\":\"test@example.com\"}")
                .build();
        UserCreatedEvent userCreatedEvent = UserCreatedEvent.builder()
                .userId(1L)
                .username("testuser")
                .email("test@example.com")
                .build();

        when(objectMapper.readValue(eq(event.getPayload()), eq(UserCreatedEvent.class))).thenReturn(userCreatedEvent);

        customerEventConsumer.handleCustomerCreatedEvent(event);

        verify(accountService).createAccount(1L, AccountType.SAVINGS);
    }
}
