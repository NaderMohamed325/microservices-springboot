package com.neo.accountservice.dto.account.input;

import com.neo.accountservice.enums.AccountType;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateAccountInputDto {
    private Long customerId; // only used/allowed for ADMIN

    @NotNull
    private AccountType accountType;
}
