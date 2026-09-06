package com.neo.accountservice.entity;


import com.neo.accountservice.enums.AggregateType;
import com.neo.accountservice.enums.EventType;
import com.neo.accountservice.enums.OutBoxStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "events")
@Getter
@Setter
@Builder
@NoArgsConstructor
@ToString
@AllArgsConstructor
public class Event extends BaseEntityAudit{

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private EventType eventType;

    @Column(nullable = false)
    private AggregateType aggregateType;

    @Column(nullable = false)
    private Long aggregateId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private OutBoxStatus status= OutBoxStatus.PENDING;
}
