package com.neo.accountservice.services.account;

import com.neo.accountservice.dto.account.input.UpdateAccountInputDto;
import com.neo.accountservice.entity.Account;
import com.neo.accountservice.entity.CustomerAccountCounter;
import com.neo.accountservice.entity.CustomerStatus;
import com.neo.accountservice.enums.AccountStatus;
import com.neo.accountservice.enums.AccountType;
import com.neo.accountservice.enums.CustomerStatusEnum;
import com.neo.accountservice.exceptions.*;
import com.neo.accountservice.repository.AccountRepository;
import com.neo.accountservice.repository.CustomerAccountCounterRepository;
import com.neo.accountservice.repository.CustomerStatusRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final CustomerAccountCounterRepository customerAccountCounterRepository;
    private final CustomerStatusRepository customerStatusRepository;

    @Override
    @Transactional
    public Account createAccount(Long customerId, AccountType accountType) {

        log.info("Creating account for customerId: {} with type: {}", customerId, accountType);

        customerStatusRepository.ensureActiveStatusExists(customerId);

        CustomerStatus customerStatus = customerStatusRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("CustomerStatus not found for customerId: " + customerId));

        if (customerStatus.getStatus() != CustomerStatusEnum.ACTIVE) {
            log.error("Cannot create account for inactive customerId: {}", customerId);
            throw new CustomerInactiveException("Cannot create account for inactive customerId: " + customerId);
        }

        AccountType resolvedType = accountType != null ? accountType : AccountType.SAVINGS;
        if (resolvedType == AccountType.SALARY) {
            long salaryCount = accountRepository.countByCustomerIdAndAccountType(customerId, AccountType.SALARY);
            if (salaryCount > 0) {
                log.error("Customer {} already has a SALARY account", customerId);
                throw new SalaryAccountAlreadyExistsException("Customer already has a SALARY account: " + customerId);
            }
        }

        customerAccountCounterRepository.ensureCounterExists(customerId);
        CustomerAccountCounter counter = customerAccountCounterRepository.findByIdForUpdate(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("CustomerAccountCounter not found for customerId: " + customerId));

        long newSeqNo = counter.getLastSeqNo() + 1;
        if (newSeqNo > 9) {
            log.error("Maximum number of accounts reached for customerId: {}", customerId);
            throw new MaxAccountsReachedException("Maximum number of accounts reached for customerId: " + customerId);
        }

        counter.setLastSeqNo(newSeqNo);
        customerAccountCounterRepository.save(counter);

        Long accountNumber = customerId * 1000 + newSeqNo;

        Account account = Account.builder()
                .id(accountNumber)
                .customerId(customerId)
                .seqNo(newSeqNo)
                .accountType(resolvedType)
                .build();

        log.info("Account created with id: {} for customerId: {}", accountNumber, customerId);
        return accountRepository.save(account);
    }

    @Override
    public Account getAccountById(Long accountId) {
        log.info("Fetching account with id: {}", accountId);
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> {
                    log.error("Account not found with id: {}", accountId);
                    return new AccountNotFoundException("Account not found with id: " + accountId);
                });
        validateAccountActive(account);
        return account;
    }

    @Override
    public Page<Account> getAccountsByCustomerId(Long customerId, int page, int size, String sortBy, String sortDir) {
        log.info("Fetching accounts for customerId: {} - page: {}, size: {}", customerId, page, size);
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return accountRepository.findAll(pageable);
    }

    @Override
    public Page<Account> getAllAccounts(int page, int size, String sortBy, String sortDir) {
        log.info("Fetching all accounts - page: {}, size: {}, sortBy: {}, sortDir: {}", page, size, sortBy, sortDir);
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return accountRepository.findAll(pageable);
    }

    @Override
    @Transactional
    public Account updateAccount(Long accountId, UpdateAccountInputDto dto) {
        log.info("Updating account with id: {}", accountId);
        Account account = getAccountById(accountId);
        validateAccountActive(account);

        if (dto.getBalance() != null) {
            account.setBalance(dto.getBalance());
        }
        if (dto.getStatus() != null) {
            account.setStatus(dto.getStatus());
        }
        if (dto.getAccountType() != null) {
            AccountType newType = dto.getAccountType();
            if (newType == AccountType.SALARY && account.getAccountType() != AccountType.SALARY) {
                long salaryCount = accountRepository.countByCustomerIdAndAccountType(account.getCustomerId(), AccountType.SALARY);
                if (salaryCount > 0) {
                    log.error("Customer {} already has a SALARY account", account.getCustomerId());
                    throw new SalaryAccountAlreadyExistsException("Customer already has a SALARY account: " + account.getCustomerId());
                }
            }
            account.setAccountType(newType);
        }

        log.info("Account updated with id: {}", accountId);
        return accountRepository.save(account);
    }

    @Override
    @Transactional
    public void deleteAccount(Long accountId) {
        log.info("Deleting account with id: {}", accountId);
        Account account = getAccountById(accountId);
        validateAccountActive(account);
        accountRepository.delete(account);
        log.info("Account deleted with id: {}", accountId);
    }

    @Override
    @Transactional
    public void deleteAccountsByCustomerId(Long customerId) {
        List<Account> accounts = accountRepository.findAllByCustomerId(customerId);
        if (accounts.isEmpty()) {
            log.warn("No accounts found for customerId: {}", customerId);
            return;
        }
        accountRepository.deleteAll(accounts);
        log.info("Deleted {} accounts for customerId: {}", accounts.size(), customerId);
    }

    @Override
    @Transactional
    public void updateCustomerStatus(Long customerId, boolean enabled) {
        CustomerStatus customerStatus = customerStatusRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("CustomerStatus not found for customerId: " + customerId));

        CustomerStatusEnum newStatus = enabled ? CustomerStatusEnum.ACTIVE : CustomerStatusEnum.SUSPENDED;
        customerStatus.setStatus(newStatus);
        customerStatus.setUpdatedAt(LocalDateTime.now());
        customerStatusRepository.save(customerStatus);

        List<Account> accounts = accountRepository.findAllByCustomerId(customerId);
        AccountStatus newAccountStatus = enabled ? AccountStatus.ACTIVE : AccountStatus.SUSPENDED;
        for (Account account : accounts) {
            account.setStatus(newAccountStatus);
        }
        accountRepository.saveAll(accounts);
        log.info("Updated status to {} for {} accounts of customerId: {}", newStatus, accounts.size(), customerId);
    }

    private void validateAccountActive(Account account) {
        if (account.getStatus() == AccountStatus.SUSPENDED) {
            throw new AccountSuspendedException("Account is suspended: " + account.getId());
        }
    }
}
