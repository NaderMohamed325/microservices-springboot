package com.neo.customerservice.services.outbox;

import com.neo.customerservice.entity.Event;
import com.neo.customerservice.enums.AggregateType;
import com.neo.customerservice.enums.EventType;
import com.neo.customerservice.enums.OutBoxStatus;
import com.neo.customerservice.repository.EventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutboxPublishServiceImplTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private OutboxPublishServiceImpl outboxPublishService;

    private Event createTestEvent(UUID id, EventType eventType) {
        return Event.builder()
                .id(id)
                .eventType(eventType)
                .aggregateType(AggregateType.USER_CUSTOMER)
                .aggregateId(1L)
                .payload("{\"userId\":1}")
                .status(OutBoxStatus.PENDING)
                .build();
    }

    @Test
    void publishOutboxEvents_noPendingEvents_doesNothing() {
        when(eventRepository.findByStatusOrderByCreatedDateAsc(OutBoxStatus.PENDING.name(), 100))
                .thenReturn(Collections.emptyList());

        outboxPublishService.publishOutboxEvents();

        verify(kafkaTemplate, never()).send(any(), any());
    }

    @Test
    void publishOutboxEvents_withPendingEvents_publishesAndMarksProcessed() throws Exception {
        Event event = createTestEvent(UUID.randomUUID(), EventType.USER_CUSTOMER_CREATED);
        when(eventRepository.findByStatusOrderByCreatedDateAsc(OutBoxStatus.PENDING.name(), 100))
                .thenReturn(List.of(event));
        when(kafkaTemplate.send(eq(AggregateType.USER_CUSTOMER.name()), any(Event.class)))
                .thenReturn(CompletableFuture.completedFuture(mock(SendResult.class)));
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));

        outboxPublishService.publishOutboxEvents();

        verify(kafkaTemplate).send(eq(AggregateType.USER_CUSTOMER.name()), eq(event));
        assertThat(event.getStatus()).isEqualTo(OutBoxStatus.PROCESSED);
        verify(eventRepository).save(event);
    }

    @Test
    void publishOutboxEvents_publishFails_eventRemainsPending() throws Exception {
        Event event = createTestEvent(UUID.randomUUID(), EventType.USER_CUSTOMER_CREATED);
        when(eventRepository.findByStatusOrderByCreatedDateAsc(OutBoxStatus.PENDING.name(), 100))
                .thenReturn(List.of(event));
        when(kafkaTemplate.send(eq(AggregateType.USER_CUSTOMER.name()), any(Event.class)))
                .thenThrow(new RuntimeException("Kafka unavailable"));

        outboxPublishService.publishOutboxEvents();

        assertThat(event.getStatus()).isEqualTo(OutBoxStatus.PENDING);
        verify(eventRepository, never()).save(any());
    }

    @Test
    void publishOutboxEvents_multipleEvents_publishesAll() throws Exception {
        Event event1 = createTestEvent(UUID.randomUUID(), EventType.USER_CUSTOMER_CREATED);
        Event event2 = createTestEvent(UUID.randomUUID(), EventType.USER_CUSTOMER_DELETED);
        when(eventRepository.findByStatusOrderByCreatedDateAsc(OutBoxStatus.PENDING.name(), 100))
                .thenReturn(List.of(event1, event2));
        when(kafkaTemplate.send(any(String.class), any(Event.class)))
                .thenReturn(CompletableFuture.completedFuture(mock(SendResult.class)));
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));

        outboxPublishService.publishOutboxEvents();

        verify(kafkaTemplate, times(2)).send(any(String.class), any(Event.class));
        verify(eventRepository, times(2)).save(any(Event.class));
    }
}
