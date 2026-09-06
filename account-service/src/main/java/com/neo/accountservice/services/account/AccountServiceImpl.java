package com.neo.accountservice.services.account;

import com.neo.accountservice.dto.account.input.UpdateAccountInputDto;
import com.neo.accountservice.entity.Account;
import com.neo.accountservice.entity.CustomerAccountCounter;
import com.neo.accountservice.entity.CustomerStatus;
import com.neo.accountservice.enums.AccountType;
import com.neo.accountservice.enums.CustomerStatusEnum;
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
                .orElseThrow(() -> new RuntimeException("CustomerStatus not found for customerId: " + customerId));

        if (customerStatus.getStatus() != CustomerStatusEnum.ACTIVE) {
            log.error("Cannot create account for inactive customerId: {}", customerId);
            throw new RuntimeException("Cannot create account for inactive customerId: " + customerId);
        }

        customerAccountCounterRepository.ensureCounterExists(customerId);
        CustomerAccountCounter counter = customerAccountCounterRepository.findByIdForUpdate(customerId)
                .orElseThrow(() -> new RuntimeException("CustomerAccountCounter not found for customerId: " + customerId));

        long newSeqNo = counter.getLastSeqNo() + 1;
        if (newSeqNo > 9) {
            log.error("Maximum number of accounts reached for customerId: {}", customerId);
            throw new RuntimeException("Maximum number of accounts reached for customerId: " + customerId);
        }

        counter.setLastSeqNo(newSeqNo);
        customerAccountCounterRepository.save(counter);

        Long accountNumber = customerId * 1000 + newSeqNo;

        Account account = Account.builder()
                .id(accountNumber)
                .customerId(customerId)
                .seqNo(newSeqNo)
                .accountType(accountType != null ? accountType : AccountType.SAVINGS)
                .build();

        log.info("Account created with id: {} for customerId: {}", accountNumber, customerId);
        return accountRepository.save(account);
    }

    @Override
    public Account getAccountById(Long accountId) {
        log.info("Fetching account with id: {}", accountId);
        return accountRepository.findById(accountId)
                .orElseThrow(() -> {
                    log.error("Account not found with id: {}", accountId);
                    return new RuntimeException("Account not found with id: " + accountId);
                });
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

        if (dto.getBalance() != null) {
            account.setBalance(dto.getBalance());
        }
        if (dto.getStatus() != null) {
            account.setStatus(dto.getStatus());
        }
        if (dto.getAccountType() != null) {
            account.setAccountType(dto.getAccountType());
        }

        log.info("Account updated with id: {}", accountId);
        return accountRepository.save(account);
    }

    @Override
    @Transactional
    public void deleteAccount(Long accountId) {
        log.info("Deleting account with id: {}", accountId);
        Account account = getAccountById(accountId);
        accountRepository.delete(account);
        log.info("Account deleted with id: {}", accountId);
    }
}
