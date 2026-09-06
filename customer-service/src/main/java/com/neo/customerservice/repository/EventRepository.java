package com.neo.customerservice.repository;

import com.neo.customerservice.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;


@Repository
public interface EventRepository extends JpaRepository<Event, UUID> {


    @Query(value = """
               SELECT * FROM events e
               WHERE e.status = :status
               ORDER BY e.created_date ASC
               LIMIT :limit
               FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    List<Event> findByStatusOrderByCreatedDateAsc(@Param("status") String status, @Param("limit") int limit);


}
