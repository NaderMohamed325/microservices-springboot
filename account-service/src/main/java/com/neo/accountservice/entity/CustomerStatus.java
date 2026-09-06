package com.neo.accountservice.entity;


import com.neo.accountservice.enums.CustomerStatusEnum;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "customer_status")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerStatus {
    @Id
    @Column(name = "customer_id")
    private Long customerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CustomerStatusEnum status; // ACTIVE, DELETED

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
