package com.neo.accountservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "customer_account_counter")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerAccountCounter {
    @Id
    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "last_seq_no", nullable = false)
    private Long lastSeqNo;
}