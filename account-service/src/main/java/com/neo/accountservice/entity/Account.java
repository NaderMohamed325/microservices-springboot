package com.neo.accountservice.entity;

import com.neo.accountservice.enums.AccountStatus;
import com.neo.accountservice.enums.AccountType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "accounts",
        uniqueConstraints = @UniqueConstraint(columnNames = {"customer_id", "seq_no"}))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Account extends BaseEntityAudit {

    @Id
    @Column(name = "id", updatable = false)
    private Long id;

    @Column(name = "customer_id", nullable = false, updatable = false)
    private Long customerId;

    @Column(name = "seq_no", nullable = false, updatable = false)
    private Long seqNo;

    @Column(nullable = false)
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AccountStatus status = AccountStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AccountType accountType = AccountType.SAVINGS;
}
