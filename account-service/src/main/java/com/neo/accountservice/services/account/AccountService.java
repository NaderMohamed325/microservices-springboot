package com.neo.accountservice.services.account;

import com.neo.accountservice.dto.account.input.UpdateAccountInputDto;
import com.neo.accountservice.entity.Account;
import com.neo.accountservice.enums.AccountType;
import org.springframework.data.domain.Page;

public interface AccountService {

    Account createAccount(Long customerId, AccountType accountType);

    Account getAccountById(Long accountId);

    Page<Account> getAccountsByCustomerId(Long customerId, int page, int size, String sortBy, String sortDir);

    Page<Account> getAllAccounts(int page, int size, String sortBy, String sortDir);

    Account updateAccount(Long accountId, UpdateAccountInputDto dto);

    void deleteAccount(Long accountId);

}
