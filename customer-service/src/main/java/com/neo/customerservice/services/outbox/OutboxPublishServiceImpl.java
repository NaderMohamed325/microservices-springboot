package com.neo.customerservice.services.outbox;

import com.neo.customerservice.entity.Event;
import com.neo.customerservice.enums.OutBoxStatus;
import com.neo.customerservice.repository.EventRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class OutboxPublishServiceImpl implements OutboxPublishService {

    private final EventRepository eventRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;


    @Override
    @Transactional
    public void publishOutboxEvents() {

        var events = eventRepository
                .findByStatusOrderByCreatedDateAsc(
                        OutBoxStatus.PENDING.name(),
                        100
                );

        if (events.isEmpty()) {
            log.info("No pending events to publish.");
            return;
        }

        for (Event event : events) {
            try {
                kafkaTemplate
                        .send(event.getAggregateType().name(), event)
                        .get();
                event.setStatus(OutBoxStatus.PROCESSED);
                eventRepository.save(event);

                log.info("Published event: {}", event);
            } catch (Exception e) {
                log.error(
                        "Failed to publish event: {}",
                        event.getId(),
                        e
                );
            }
        }
    }
}
