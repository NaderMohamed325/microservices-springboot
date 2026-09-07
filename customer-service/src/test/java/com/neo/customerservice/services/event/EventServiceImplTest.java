package com.neo.customerservice.services.event;

import com.neo.customerservice.entity.Event;
import com.neo.customerservice.enums.AggregateType;
import com.neo.customerservice.enums.EventType;
import com.neo.customerservice.repository.EventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventServiceImplTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private EventServiceImpl eventService;

    @Test
    void saveEvent_validEvent_savesEvent() throws Exception {
        String payload = "{\"userId\":1,\"username\":\"testuser\"}";
        when(objectMapper.writeValueAsString(any())).thenReturn(payload);
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));

        eventService.saveEvent(
                EventType.USER_CUSTOMER_CREATED,
                AggregateType.USER_CUSTOMER,
                1L,
                payload
        );

        verify(eventRepository).save(any(Event.class));
    }

    @Test
    void saveEvent_setsCorrectEventType() throws Exception {
        String payload = "{\"userId\":1}";
        when(objectMapper.writeValueAsString(any())).thenReturn(payload);
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> {
            Event saved = invocation.getArgument(0);
            assertThat(saved.getEventType()).isEqualTo(EventType.USER_CUSTOMER_CREATED);
            return saved;
        });

        eventService.saveEvent(
                EventType.USER_CUSTOMER_CREATED,
                AggregateType.USER_CUSTOMER,
                1L,
                payload
        );

        verify(eventRepository).save(any(Event.class));
    }

    @Test
    void saveEvent_setsCorrectAggregateType() throws Exception {
        String payload = "{\"userId\":1}";
        when(objectMapper.writeValueAsString(any())).thenReturn(payload);
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> {
            Event saved = invocation.getArgument(0);
            assertThat(saved.getAggregateType()).isEqualTo(AggregateType.USER_CUSTOMER);
            return saved;
        });

        eventService.saveEvent(
                EventType.USER_CUSTOMER_CREATED,
                AggregateType.USER_CUSTOMER,
                1L,
                payload
        );

        verify(eventRepository).save(any(Event.class));
    }

    @Test
    void saveEvent_setsCorrectAggregateId() throws Exception {
        String payload = "{\"userId\":1}";
        when(objectMapper.writeValueAsString(any())).thenReturn(payload);
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> {
            Event saved = invocation.getArgument(0);
            assertThat(saved.getAggregateId()).isEqualTo(1L);
            return saved;
        });

        eventService.saveEvent(
                EventType.USER_CUSTOMER_CREATED,
                AggregateType.USER_CUSTOMER,
                1L,
                payload
        );

        verify(eventRepository).save(any(Event.class));
    }

    @Test
    void saveEvent_setsPayload() throws Exception {
        String payload = "{\"userId\":1,\"username\":\"testuser\"}";
        when(objectMapper.writeValueAsString(any())).thenReturn(payload);
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> {
            Event saved = invocation.getArgument(0);
            assertThat(saved.getPayload()).isEqualTo(payload);
            return saved;
        });

        eventService.saveEvent(
                EventType.USER_CUSTOMER_CREATED,
                AggregateType.USER_CUSTOMER,
                1L,
                payload
        );

        verify(eventRepository).save(any(Event.class));
    }
}
