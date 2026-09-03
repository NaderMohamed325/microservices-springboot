package com.neo.customerservice.repository;

import com.neo.customerservice.entity.User;
import com.neo.customerservice.enums.UserRoles;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Page<User> findAllByRole(UserRoles role, Pageable pageable);


    @Modifying(
            flushAutomatically = true,
            clearAutomatically = true
    )
    @Query("DELETE FROM User u WHERE u.id = :id")
    void deleteUserById(Long id);
}
