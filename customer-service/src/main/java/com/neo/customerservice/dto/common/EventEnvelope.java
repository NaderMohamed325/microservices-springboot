package com.neo.customerservice.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.Instant;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO for sending events with metadata")
public class EventEnvelope<T> {
    String eventType;
    String eventId;
    Instant eventTimestamp;
    T data;
}
