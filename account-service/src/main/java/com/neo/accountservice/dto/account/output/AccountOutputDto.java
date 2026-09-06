package com.neo.accountservice.dto.account.output;

import com.neo.accountservice.enums.AccountStatus;
import com.neo.accountservice.enums.AccountType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountOutputDto {
    private Long id;
    private Long customerId;
    private Long seqNo;
    private BigDecimal balance;
    private AccountStatus status;
    private AccountType accountType;
    private LocalDateTime createdDate;
}
