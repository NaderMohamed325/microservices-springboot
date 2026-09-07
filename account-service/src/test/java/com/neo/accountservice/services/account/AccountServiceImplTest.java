package com.neo.accountservice.services.account;

import com.neo.accountservice.dto.account.input.UpdateAccountInputDto;
import com.neo.accountservice.entity.Account;
import com.neo.accountservice.entity.CustomerAccountCounter;
import com.neo.accountservice.entity.CustomerStatus;
import com.neo.accountservice.enums.AccountStatus;
import com.neo.accountservice.enums.AccountType;
import com.neo.accountservice.enums.CustomerStatusEnum;
import com.neo.accountservice.exceptions.AccountNotFoundException;
import com.neo.accountservice.exceptions.CustomerInactiveException;
import com.neo.accountservice.exceptions.MaxAccountsReachedException;
import com.neo.accountservice.exceptions.SalaryAccountAlreadyExistsException;
import com.neo.accountservice.repository.AccountRepository;
import com.neo.accountservice.repository.CustomerAccountCounterRepository;
import com.neo.accountservice.repository.CustomerStatusRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CustomerAccountCounterRepository customerAccountCounterRepository;

    @Mock
    private CustomerStatusRepository customerStatusRepository;

    @InjectMocks
    private AccountServiceImpl accountService;

    private Account testAccount;

    @BeforeEach
    void setUp() {
        testAccount = Account.builder()
                .id(1001L)
                .customerId(1L)
                .seqNo(1L)
                .balance(BigDecimal.ZERO)
                .status(AccountStatus.ACTIVE)
                .accountType(AccountType.SAVINGS)
                .build();
    }

    @Test
    void createAccount_activeCustomer_returnsAccount() {
        CustomerStatus status = CustomerStatus.builder()
                .customerId(1L)
                .status(CustomerStatusEnum.ACTIVE)
                .build();
        CustomerAccountCounter counter = CustomerAccountCounter.builder()
                .customerId(1L)
                .lastSeqNo(0L)
                .build();

        when(customerStatusRepository.findById(1L)).thenReturn(Optional.of(status));
        when(customerAccountCounterRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(counter));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        Account result = accountService.createAccount(1L, AccountType.SAVINGS);

        assertThat(result).isNotNull();
        assertThat(result.getCustomerId()).isEqualTo(1L);
        verify(customerStatusRepository).ensureActiveStatusExists(1L);
        verify(customerAccountCounterRepository).ensureCounterExists(1L);
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void createAccount_inactiveCustomer_throwsException() {
        CustomerStatus status = CustomerStatus.builder()
                .customerId(1L)
                .status(CustomerStatusEnum.SUSPENDED)
                .build();

        when(customerStatusRepository.findById(1L)).thenReturn(Optional.of(status));

        assertThatThrownBy(() -> accountService.createAccount(1L, AccountType.SAVINGS))
                .isInstanceOf(CustomerInactiveException.class)
                .hasMessageContaining("Cannot create account for inactive customerId");

        verify(accountRepository, never()).save(any());
    }

    @Test
    void createAccount_maxAccountsReached_throwsException() {
        CustomerStatus status = CustomerStatus.builder()
                .customerId(1L)
                .status(CustomerStatusEnum.ACTIVE)
                .build();
        CustomerAccountCounter counter = CustomerAccountCounter.builder()
                .customerId(1L)
                .lastSeqNo(9L)
                .build();

        when(customerStatusRepository.findById(1L)).thenReturn(Optional.of(status));
        when(customerAccountCounterRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(counter));

        assertThatThrownBy(() -> accountService.createAccount(1L, AccountType.SAVINGS))
                .isInstanceOf(MaxAccountsReachedException.class)
                .hasMessageContaining("Maximum number of accounts reached");

        verify(accountRepository, never()).save(any());
    }

    @Test
    void createAccount_nullAccountType_defaultsToSavings() {
        CustomerStatus status = CustomerStatus.builder()
                .customerId(1L)
                .status(CustomerStatusEnum.ACTIVE)
                .build();
        CustomerAccountCounter counter = CustomerAccountCounter.builder()
                .customerId(1L)
                .lastSeqNo(0L)
                .build();
        Account savedAccount = Account.builder()
                .id(1001L)
                .customerId(1L)
                .seqNo(1L)
                .accountType(AccountType.SAVINGS)
                .build();

        when(customerStatusRepository.findById(1L)).thenReturn(Optional.of(status));
        when(customerAccountCounterRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(counter));
        when(accountRepository.save(any(Account.class))).thenReturn(savedAccount);

        Account result = accountService.createAccount(1L, null);

        assertThat(result).isNotNull();
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void getAccountById_accountExists_returnsAccount() {
        when(accountRepository.findById(1001L)).thenReturn(Optional.of(testAccount));

        Account result = accountService.getAccountById(1001L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1001L);
        assertThat(result.getCustomerId()).isEqualTo(1L);
    }

    @Test
    void getAccountById_accountNotFound_throwsException() {
        when(accountRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.getAccountById(999L))
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessageContaining("Account not found with id: 999");
    }

    @Test
    void getAllAccounts_validPage_returnsPage() {
        Page<Account> page = new PageImpl<>(List.of(testAccount), PageRequest.of(0, 10, Sort.by("id").ascending()), 1);
        when(accountRepository.findAll(any(PageRequest.class))).thenReturn(page);

        Page<Account> result = accountService.getAllAccounts(0, 10, "id", "asc");

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(1001L);
    }

    @Test
    void getAccountsByCustomerId_validCustomer_returnsPage() {
        Page<Account> page = new PageImpl<>(List.of(testAccount), PageRequest.of(0, 10, Sort.by("id").ascending()), 1);
        when(accountRepository.findAll(any(PageRequest.class))).thenReturn(page);

        Page<Account> result = accountService.getAccountsByCustomerId(1L, 0, 10, "id", "asc");

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void updateAccount_accountExists_returnsUpdatedAccount() {
        when(accountRepository.findById(1001L)).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        UpdateAccountInputDto dto = UpdateAccountInputDto.builder()
                .balance(new BigDecimal("1000.00"))
                .status(AccountStatus.ACTIVE)
                .accountType(AccountType.INVESTMENT)
                .build();

        Account result = accountService.updateAccount(1001L, dto);

        assertThat(result).isNotNull();
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void updateAccount_partialUpdate_onlyUpdatesProvidedFields() {
        when(accountRepository.findById(1001L)).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        UpdateAccountInputDto dto = UpdateAccountInputDto.builder()
                .balance(new BigDecimal("500.00"))
                .build();

        Account result = accountService.updateAccount(1001L, dto);

        assertThat(result).isNotNull();
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void deleteAccount_accountExists_deletesSuccessfully() {
        when(accountRepository.findById(1001L)).thenReturn(Optional.of(testAccount));

        accountService.deleteAccount(1001L);

        verify(accountRepository).delete(testAccount);
    }

    @Test
    void deleteAccount_accountNotFound_throwsException() {
        when(accountRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.deleteAccount(999L))
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessageContaining("Account not found with id: 999");

        verify(accountRepository, never()).delete(any());
    }

    @Test
    void createAccount_salaryWhenNoneExists_returnsAccount() {
        CustomerStatus status = CustomerStatus.builder()
                .customerId(1L)
                .status(CustomerStatusEnum.ACTIVE)
                .build();
        CustomerAccountCounter counter = CustomerAccountCounter.builder()
                .customerId(1L)
                .lastSeqNo(0L)
                .build();
        Account salaryAccount = Account.builder()
                .id(1001L)
                .customerId(1L)
                .seqNo(1L)
                .accountType(AccountType.SALARY)
                .build();

        when(customerStatusRepository.findById(1L)).thenReturn(Optional.of(status));
        when(customerAccountCounterRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(counter));
        when(accountRepository.countByCustomerIdAndAccountType(1L, AccountType.SALARY)).thenReturn(0L);
        when(accountRepository.save(any(Account.class))).thenReturn(salaryAccount);

        Account result = accountService.createAccount(1L, AccountType.SALARY);

        assertThat(result).isNotNull();
        assertThat(result.getAccountType()).isEqualTo(AccountType.SALARY);
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void createAccount_salaryWhenOneExists_throwsException() {
        CustomerStatus status = CustomerStatus.builder()
                .customerId(1L)
                .status(CustomerStatusEnum.ACTIVE)
                .build();

        when(customerStatusRepository.findById(1L)).thenReturn(Optional.of(status));
        when(accountRepository.countByCustomerIdAndAccountType(1L, AccountType.SALARY)).thenReturn(1L);

        assertThatThrownBy(() -> accountService.createAccount(1L, AccountType.SALARY))
                .isInstanceOf(SalaryAccountAlreadyExistsException.class)
                .hasMessageContaining("Customer already has a SALARY account");

        verify(accountRepository, never()).save(any());
    }

    @Test
    void updateAccount_toSalaryWhenNoneExists_succeeds() {
        Account existingAccount = Account.builder()
                .id(1001L)
                .customerId(1L)
                .seqNo(1L)
                .accountType(AccountType.SAVINGS)
                .build();

        when(accountRepository.findById(1001L)).thenReturn(Optional.of(existingAccount));
        when(accountRepository.countByCustomerIdAndAccountType(1L, AccountType.SALARY)).thenReturn(0L);
        when(accountRepository.save(any(Account.class))).thenReturn(existingAccount);

        UpdateAccountInputDto dto = UpdateAccountInputDto.builder()
                .accountType(AccountType.SALARY)
                .build();

        Account result = accountService.updateAccount(1001L, dto);

        assertThat(result).isNotNull();
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void updateAccount_toSalaryWhenOneExists_throwsException() {
        Account existingAccount = Account.builder()
                .id(1001L)
                .customerId(1L)
                .seqNo(1L)
                .accountType(AccountType.SAVINGS)
                .build();

        when(accountRepository.findById(1001L)).thenReturn(Optional.of(existingAccount));
        when(accountRepository.countByCustomerIdAndAccountType(1L, AccountType.SALARY)).thenReturn(1L);

        UpdateAccountInputDto dto = UpdateAccountInputDto.builder()
                .accountType(AccountType.SALARY)
                .build();

        assertThatThrownBy(() -> accountService.updateAccount(1001L, dto))
                .isInstanceOf(SalaryAccountAlreadyExistsException.class)
                .hasMessageContaining("Customer already has a SALARY account");

        verify(accountRepository, never()).save(any());
    }
}
