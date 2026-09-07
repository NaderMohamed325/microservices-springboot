package com.neo.customerservice.entity;

import com.neo.customerservice.config.JpaAuditingConfiguration;
import com.neo.customerservice.enums.AggregateType;
import com.neo.customerservice.enums.EventType;
import com.neo.customerservice.enums.OutBoxStatus;
import com.neo.customerservice.repository.EventRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import(JpaAuditingConfiguration.class)
class EventEntityTest {

    @Autowired
    private EventRepository eventRepository;

    private Event createTestEvent(EventType eventType, AggregateType aggregateType, Long aggregateId) {
        return Event.builder()
                .eventType(eventType)
                .aggregateType(aggregateType)
                .aggregateId(aggregateId)
                .payload("{\"test\":\"data\"}")
                .status(OutBoxStatus.PENDING)
                .build();
    }

    @Test
    void saveEvent_withValidData_persistsAllFields() {
        Event event = createTestEvent(EventType.USER_CUSTOMER_CREATED, AggregateType.USER_CUSTOMER, 1L);
        Event saved = eventRepository.save(event);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getEventType()).isEqualTo(EventType.USER_CUSTOMER_CREATED);
        assertThat(saved.getAggregateType()).isEqualTo(AggregateType.USER_CUSTOMER);
        assertThat(saved.getAggregateId()).isEqualTo(1L);
        assertThat(saved.getPayload()).isEqualTo("{\"test\":\"data\"}");
        assertThat(saved.getStatus()).isEqualTo(OutBoxStatus.PENDING);
    }

    @Test
    void saveEvent_allEventTypes_persistsCorrectly() {
        for (EventType type : EventType.values()) {
            Event event = createTestEvent(type, AggregateType.USER_CUSTOMER, 1L);
            Event saved = eventRepository.save(event);
            assertThat(saved.getEventType()).isEqualTo(type);
        }
    }

    @Test
    void saveEvent_allAggregateTypes_persistsCorrectly() {
        for (AggregateType type : AggregateType.values()) {
            Event event = createTestEvent(EventType.USER_CUSTOMER_CREATED, type, 1L);
            Event saved = eventRepository.save(event);
            assertThat(saved.getAggregateType()).isEqualTo(type);
        }
    }

    @Test
    void saveEvent_allStatuses_persistsCorrectly() {
        for (OutBoxStatus status : OutBoxStatus.values()) {
            Event event = Event.builder()
                    .eventType(EventType.USER_CUSTOMER_CREATED)
                    .aggregateType(AggregateType.USER_CUSTOMER)
                    .aggregateId(1L)
                    .payload("{\"test\":\"data\"}")
                    .status(status)
                    .build();
            Event saved = eventRepository.save(event);
            assertThat(saved.getStatus()).isEqualTo(status);
        }
    }

    @Test
    void saveEvent_defaultStatusIsPending() {
        Event event = Event.builder()
                .eventType(EventType.USER_CUSTOMER_CREATED)
                .aggregateType(AggregateType.USER_CUSTOMER)
                .aggregateId(1L)
                .payload("{\"test\":\"data\"}")
                .build();
        Event saved = eventRepository.save(event);

        assertThat(saved.getStatus()).isEqualTo(OutBoxStatus.PENDING);
    }

    @Test
    void saveEvent_nullEventType_throwsException() {
        Event event = Event.builder()
                .aggregateType(AggregateType.USER_CUSTOMER)
                .aggregateId(1L)
                .payload("{\"test\":\"data\"}")
                .status(OutBoxStatus.PENDING)
                .build();

        assertThatThrownBy(() -> {
            eventRepository.save(event);
            eventRepository.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void saveEvent_nullAggregateType_throwsException() {
        Event event = Event.builder()
                .eventType(EventType.USER_CUSTOMER_CREATED)
                .aggregateId(1L)
                .payload("{\"test\":\"data\"}")
                .status(OutBoxStatus.PENDING)
                .build();

        assertThatThrownBy(() -> {
            eventRepository.save(event);
            eventRepository.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void saveEvent_nullAggregateId_throwsException() {
        Event event = Event.builder()
                .eventType(EventType.USER_CUSTOMER_CREATED)
                .aggregateType(AggregateType.USER_CUSTOMER)
                .payload("{\"test\":\"data\"}")
                .status(OutBoxStatus.PENDING)
                .build();

        assertThatThrownBy(() -> {
            eventRepository.save(event);
            eventRepository.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void saveEvent_nullPayload_throwsException() {
        Event event = Event.builder()
                .eventType(EventType.USER_CUSTOMER_CREATED)
                .aggregateType(AggregateType.USER_CUSTOMER)
                .aggregateId(1L)
                .status(OutBoxStatus.PENDING)
                .build();

        assertThatThrownBy(() -> {
            eventRepository.save(event);
            eventRepository.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }
}
