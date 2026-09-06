package com.neo.customerservice.services.event;

import com.neo.customerservice.enums.AggregateType;
import com.neo.customerservice.enums.EventType;

public interface EventService {
    <T> void saveEvent(EventType eventType, AggregateType aggregateType, Long aggregateId, T event);
}
