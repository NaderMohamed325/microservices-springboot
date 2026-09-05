package com.neo.customerservice.services.producer;

import com.neo.customerservice.dto.common.EventEnvelope;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProducerServiceImpl implements ProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public <T> void sendMessage(String topic, T message) {

        EventEnvelope<T> eventEnvelope =
                EventEnvelope.<T>builder()
                        .eventType(message.getClass().getSimpleName())
                        .eventId(java.util.UUID.randomUUID().toString())
                        .eventTimestamp(java.time.Instant.now())
                        .data(message)
                        .build();

        kafkaTemplate.send(topic, eventEnvelope);

        log.info(
                "Sent event {} to topic {}",
                eventEnvelope.getEventType(),
                topic
        );
    }
}