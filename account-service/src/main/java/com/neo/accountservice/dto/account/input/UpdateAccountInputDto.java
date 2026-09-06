package com.neo.accountservice.dto.account.input;

import com.neo.accountservice.enums.AccountStatus;
import com.neo.accountservice.enums.AccountType;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateAccountInputDto {
    private BigDecimal balance;
    private AccountStatus status;
    private AccountType accountType;
}
