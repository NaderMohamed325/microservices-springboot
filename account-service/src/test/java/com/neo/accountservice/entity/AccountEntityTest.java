package com.neo.accountservice.entity;

import com.neo.accountservice.config.JpaAuditingConfiguration;
import com.neo.accountservice.enums.AccountStatus;
import com.neo.accountservice.enums.AccountType;
import com.neo.accountservice.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import(JpaAuditingConfiguration.class)
class AccountEntityTest {

    @Autowired
    private AccountRepository accountRepository;

    private Account createTestAccount(Long id, Long customerId, Long seqNo) {
        return Account.builder()
                .id(id)
                .customerId(customerId)
                .seqNo(seqNo)
                .balance(BigDecimal.ZERO)
                .status(AccountStatus.ACTIVE)
                .accountType(AccountType.SAVINGS)
                .build();
    }

    @Test
    void saveAccount_withValidData_persistsAllFields() {
        Account account = createTestAccount(1001L, 1L, 1L);
        Account saved = accountRepository.save(account);

        assertThat(saved.getId()).isEqualTo(1001L);
        assertThat(saved.getCustomerId()).isEqualTo(1L);
        assertThat(saved.getSeqNo()).isEqualTo(1L);
        assertThat(saved.getBalance()).isEqualTo(BigDecimal.ZERO);
        assertThat(saved.getStatus()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(saved.getAccountType()).isEqualTo(AccountType.SAVINGS);
    }

    @Test
    void saveAccount_defaultBalanceIsZero() {
        Account account = createTestAccount(1002L, 2L, 1L);
        Account saved = accountRepository.save(account);

        assertThat(saved.getBalance()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    void saveAccount_defaultStatusIsActive() {
        Account account = createTestAccount(1003L, 3L, 1L);
        Account saved = accountRepository.save(account);

        assertThat(saved.getStatus()).isEqualTo(AccountStatus.ACTIVE);
    }

    @Test
    void saveAccount_defaultTypeIsSavings() {
        Account account = createTestAccount(1004L, 4L, 1L);
        Account saved = accountRepository.save(account);

        assertThat(saved.getAccountType()).isEqualTo(AccountType.SAVINGS);
    }

    @Test
    void saveAccount_customValues_persistsCorrectly() {
        Account account = Account.builder()
                .id(1005L)
                .customerId(5L)
                .seqNo(2L)
                .balance(new BigDecimal("1500.50"))
                .status(AccountStatus.INACTIVE)
                .accountType(AccountType.INVESTMENT)
                .build();
        Account saved = accountRepository.save(account);

        assertThat(saved.getBalance()).isEqualTo(new BigDecimal("1500.50"));
        assertThat(saved.getStatus()).isEqualTo(AccountStatus.INACTIVE);
        assertThat(saved.getAccountType()).isEqualTo(AccountType.INVESTMENT);
    }

    @Test
    void saveAccount_allAccountTypes_persistsCorrectly() {
        for (AccountType type : AccountType.values()) {
            Account account = Account.builder()
                    .id(2000L + type.ordinal())
                    .customerId(10L + type.ordinal())
                    .seqNo(1L)
                    .accountType(type)
                    .build();
            Account saved = accountRepository.save(account);
            assertThat(saved.getAccountType()).isEqualTo(type);
        }
    }

    @Test
    void saveAccount_allStatuses_persistsCorrectly() {
        for (AccountStatus status : AccountStatus.values()) {
            Account account = Account.builder()
                    .id(3000L + status.ordinal())
                    .customerId(20L + status.ordinal())
                    .seqNo(1L)
                    .status(status)
                    .build();
            Account saved = accountRepository.save(account);
            assertThat(saved.getStatus()).isEqualTo(status);
        }
    }

    @Test
    void saveAccount_duplicateId_throwsException() {
        accountRepository.save(createTestAccount(1001L, 1L, 1L));
        accountRepository.flush();

        Account duplicate = createTestAccount(1001L, 2L, 1L);

        assertThatThrownBy(() -> {
            accountRepository.save(duplicate);
            accountRepository.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void saveAccount_nullCustomerId_throwsException() {
        Account account = Account.builder()
                .id(1006L)
                .seqNo(1L)
                .accountType(AccountType.SAVINGS)
                .build();

        assertThatThrownBy(() -> {
            accountRepository.save(account);
            accountRepository.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void saveAccount_nullSeqNo_throwsException() {
        Account account = Account.builder()
                .id(1007L)
                .customerId(7L)
                .accountType(AccountType.SAVINGS)
                .build();

        assertThatThrownBy(() -> {
            accountRepository.save(account);
            accountRepository.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }
}
