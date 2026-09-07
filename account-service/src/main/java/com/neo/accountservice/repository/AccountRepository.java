package com.neo.accountservice.repository;

import com.neo.accountservice.entity.Account;
import com.neo.accountservice.enums.AccountType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    List<Account> findAllByCustomerId(Long customerId);

    long countByCustomerIdAndAccountType(Long customerId, AccountType accountType);

}
