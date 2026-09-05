package com.neo.customerservice.services.event;

import com.neo.customerservice.entity.Event;
import com.neo.customerservice.enums.AggregateType;
import com.neo.customerservice.enums.EventType;
import com.neo.customerservice.repository.EventRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public <T> void saveEvent(EventType eventType, AggregateType aggregateType, Long aggregateId, T event) {
        String payload = objectMapper.writeValueAsString(event);
        Event outboxEvent = Event.builder()
                .eventType(eventType)
                .aggregateType(aggregateType)
                .aggregateId(aggregateId)
                .payload(payload)
                .build();
        eventRepository.save(outboxEvent);
        log.info("Event saved: {}", outboxEvent);
    }
}
