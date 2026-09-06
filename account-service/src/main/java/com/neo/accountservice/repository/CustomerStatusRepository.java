package com.neo.accountservice.repository;

import com.neo.accountservice.entity.CustomerStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerStatusRepository extends JpaRepository<CustomerStatus, Long> {

    @Modifying
    @Query(value = """
        INSERT INTO customer_status (customer_id, status, updated_at)
        VALUES (:customerId, 'ACTIVE', now())
        ON CONFLICT (customer_id) DO NOTHING
        """, nativeQuery = true)
    void ensureActiveStatusExists(@Param("customerId") Long customerId);
}
