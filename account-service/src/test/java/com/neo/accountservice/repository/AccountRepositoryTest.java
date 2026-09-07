package com.neo.accountservice.repository;

import com.neo.accountservice.config.JpaAuditingConfiguration;
import com.neo.accountservice.entity.Account;
import com.neo.accountservice.enums.AccountStatus;
import com.neo.accountservice.enums.AccountType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaAuditingConfiguration.class)
class AccountRepositoryTest {

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
    void saveAccount_returnsSavedAccount() {
        Account account = createTestAccount(1001L, 1L, 1L);
        Account saved = accountRepository.save(account);

        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isEqualTo(1001L);
    }

    @Test
    void findAll_multipleAccounts_returnsAll() {
        accountRepository.save(createTestAccount(1001L, 1L, 1L));
        accountRepository.save(createTestAccount(1002L, 2L, 1L));

        List<Account> accounts = accountRepository.findAll();

        assertThat(accounts).hasSize(2);
    }

    @Test
    void findById_existingAccount_returnsAccount() {
        accountRepository.save(createTestAccount(1001L, 1L, 1L));

        Optional<Account> found = accountRepository.findById(1001L);

        assertThat(found).isPresent();
        assertThat(found.get().getCustomerId()).isEqualTo(1L);
    }

    @Test
    void findById_nonExistingAccount_returnsEmpty() {
        Optional<Account> found = accountRepository.findById(999L);

        assertThat(found).isEmpty();
    }

    @Test
    void deleteById_existingAccount_removesAccount() {
        Account account = accountRepository.save(createTestAccount(1001L, 1L, 1L));
        accountRepository.flush();

        accountRepository.deleteById(1001L);
        accountRepository.flush();

        assertThat(accountRepository.findById(1001L)).isEmpty();
    }

    @Test
    void count_noAccounts_returnsZero() {
        assertThat(accountRepository.count()).isEqualTo(0);
    }

    @Test
    void count_withAccounts_returnsCorrectCount() {
        accountRepository.save(createTestAccount(1001L, 1L, 1L));
        accountRepository.save(createTestAccount(1002L, 2L, 1L));
        accountRepository.save(createTestAccount(1003L, 3L, 1L));

        assertThat(accountRepository.count()).isEqualTo(3);
    }

    @Test
    void saveAccount_withCustomBalance_persistsBalance() {
        Account account = Account.builder()
                .id(1001L)
                .customerId(1L)
                .seqNo(1L)
                .balance(new BigDecimal("5000.75"))
                .status(AccountStatus.ACTIVE)
                .accountType(AccountType.INVESTMENT)
                .build();
        Account saved = accountRepository.save(account);

        assertThat(saved.getBalance()).isEqualTo(new BigDecimal("5000.75"));
    }

    @Test
    void existsById_existingAccount_returnsTrue() {
        accountRepository.save(createTestAccount(1001L, 1L, 1L));

        assertThat(accountRepository.existsById(1001L)).isTrue();
    }

    @Test
    void existsById_nonExistingAccount_returnsFalse() {
        assertThat(accountRepository.existsById(999L)).isFalse();
    }
}
