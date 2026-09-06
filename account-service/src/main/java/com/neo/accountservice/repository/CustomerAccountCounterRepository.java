package com.neo.accountservice.repository;

import com.neo.accountservice.entity.CustomerAccountCounter;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface CustomerAccountCounterRepository extends JpaRepository<CustomerAccountCounter, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from CustomerAccountCounter c where c.customerId = :customerId")
    Optional<CustomerAccountCounter> findByIdForUpdate(@Param("customerId") Long customerId);

    // used only to guarantee the row exists before locking it
    @Modifying
    @Query(value = """
        INSERT INTO customer_account_counter (customer_id, last_seq_no)
        VALUES (:customerId, -1)
        ON CONFLICT (customer_id) DO NOTHING
        """, nativeQuery = true)
    void ensureCounterExists(@Param("customerId") Long customerId);
}