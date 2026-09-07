package com.neo.customerservice.repository;

import com.neo.customerservice.config.JpaAuditingConfiguration;
import com.neo.customerservice.entity.Event;
import com.neo.customerservice.enums.AggregateType;
import com.neo.customerservice.enums.EventType;
import com.neo.customerservice.enums.OutBoxStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaAuditingConfiguration.class)
class EventRepositoryTest {

    @Autowired
    private EventRepository eventRepository;

    private Event createTestEvent(EventType eventType, AggregateType aggregateType, Long aggregateId, OutBoxStatus status) {
        return Event.builder()
                .eventType(eventType)
                .aggregateType(aggregateType)
                .aggregateId(aggregateId)
                .payload("{\"test\":\"data\"}")
                .status(status)
                .build();
    }

    @Test
    void saveEvent_withValidData_persistsAllFields() {
        Event event = createTestEvent(EventType.USER_CUSTOMER_CREATED, AggregateType.USER_CUSTOMER, 1L, OutBoxStatus.PENDING);
        Event saved = eventRepository.save(event);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getEventType()).isEqualTo(EventType.USER_CUSTOMER_CREATED);
        assertThat(saved.getAggregateType()).isEqualTo(AggregateType.USER_CUSTOMER);
        assertThat(saved.getAggregateId()).isEqualTo(1L);
        assertThat(saved.getPayload()).isEqualTo("{\"test\":\"data\"}");
        assertThat(saved.getStatus()).isEqualTo(OutBoxStatus.PENDING);
    }

    @Test
    void saveEvent_defaultStatusIsPending() {
        Event event = createTestEvent(EventType.USER_CUSTOMER_CREATED, AggregateType.USER_CUSTOMER, 1L, OutBoxStatus.PENDING);
        Event saved = eventRepository.save(event);

        assertThat(saved.getStatus()).isEqualTo(OutBoxStatus.PENDING);
    }

    @Test
    void findByStatusOrderByCreatedDateAsc_pendingEvents_returnsEvents() {
        eventRepository.save(createTestEvent(EventType.USER_CUSTOMER_CREATED, AggregateType.USER_CUSTOMER, 1L, OutBoxStatus.PENDING));
        eventRepository.save(createTestEvent(EventType.USER_CUSTOMER_DELETED, AggregateType.USER_CUSTOMER, 2L, OutBoxStatus.PENDING));

        List<Event> events = eventRepository.findByStatusOrderByCreatedDateAsc(OutBoxStatus.PENDING.name(), 100);

        assertThat(events).hasSize(2);
    }

    @Test
    void findByStatusOrderByCreatedDateAsc_noPendingEvents_returnsEmpty() {
        eventRepository.save(createTestEvent(EventType.USER_CUSTOMER_CREATED, AggregateType.USER_CUSTOMER, 1L, OutBoxStatus.PROCESSED));

        List<Event> events = eventRepository.findByStatusOrderByCreatedDateAsc(OutBoxStatus.PENDING.name(), 100);

        assertThat(events).isEmpty();
    }

    @Test
    void findByStatusOrderByCreatedDateAsc_respectsLimit() {
        for (int i = 0; i < 5; i++) {
            eventRepository.save(createTestEvent(EventType.USER_CUSTOMER_CREATED, AggregateType.USER_CUSTOMER, (long) i, OutBoxStatus.PENDING));
        }

        List<Event> events = eventRepository.findByStatusOrderByCreatedDateAsc(OutBoxStatus.PENDING.name(), 3);

        assertThat(events).hasSize(3);
    }

    @Test
    void findByStatusOrderByCreatedDateAsc_mixedStatuses_returnsOnlyPending() {
        eventRepository.save(createTestEvent(EventType.USER_CUSTOMER_CREATED, AggregateType.USER_CUSTOMER, 1L, OutBoxStatus.PENDING));
        eventRepository.save(createTestEvent(EventType.USER_CUSTOMER_CREATED, AggregateType.USER_CUSTOMER, 2L, OutBoxStatus.PROCESSED));
        eventRepository.save(createTestEvent(EventType.USER_CUSTOMER_CREATED, AggregateType.USER_CUSTOMER, 3L, OutBoxStatus.PENDING));

        List<Event> events = eventRepository.findByStatusOrderByCreatedDateAsc(OutBoxStatus.PENDING.name(), 100);

        assertThat(events).hasSize(2);
        assertThat(events).allMatch(e -> e.getStatus() == OutBoxStatus.PENDING);
    }

    @Test
    void count_multipleEvents_returnsCorrectCount() {
        eventRepository.save(createTestEvent(EventType.USER_CUSTOMER_CREATED, AggregateType.USER_CUSTOMER, 1L, OutBoxStatus.PENDING));
        eventRepository.save(createTestEvent(EventType.USER_CUSTOMER_DELETED, AggregateType.USER_CUSTOMER, 2L, OutBoxStatus.PROCESSED));

        assertThat(eventRepository.count()).isEqualTo(2);
    }

    @Test
    void deleteById_existingEvent_removesEvent() {
        Event event = eventRepository.save(createTestEvent(EventType.USER_CUSTOMER_CREATED, AggregateType.USER_CUSTOMER, 1L, OutBoxStatus.PENDING));
        eventRepository.flush();

        eventRepository.deleteById(event.getId());
        eventRepository.flush();

        assertThat(eventRepository.findById(event.getId())).isEmpty();
    }
}
