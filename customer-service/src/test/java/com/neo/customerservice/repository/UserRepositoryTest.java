package com.neo.customerservice.repository;

import com.neo.customerservice.config.JpaAuditingConfiguration;
import com.neo.customerservice.entity.User;
import com.neo.customerservice.enums.UserRoles;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Import(JpaAuditingConfiguration.class)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User createUser(String username, String email, String password) {
        User user = new User();

        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);
        user.setRole(UserRoles.CUSTOMER);

        return userRepository.save(user);
    }

    @Test
    void shouldFindUserByUsername() {
        createUser(
                "testuser",
                "test@test.com",
                "password"
        );

        Optional<User> foundUser =
                userRepository.findByUsername("testuser");

        assertTrue(foundUser.isPresent());
        assertEquals("testuser", foundUser.get().getUsername());
    }

    @Test
    void shouldFindUserByEmail() {
        createUser(
                "testuser",
                "test@test.com",
                "password"
        );

        Optional<User> foundUser =
                userRepository.findByEmail("test@test.com");

        assertTrue(foundUser.isPresent());
        assertEquals("test@test.com", foundUser.get().getEmail());
    }

    @Test
    void shouldDeleteUserById() {
        User savedUser = createUser(
                "testuser",
                "test@test.com",
                "password"
        );

        userRepository.deleteUserById(savedUser.getId());

        userRepository.flush();

        Optional<User> deletedUser =
                userRepository.findById(savedUser.getId());

        assertTrue(deletedUser.isEmpty());
    }

    @Test
    void shouldFindUsersWithPagination() {

        createUser("user1", "user1@test.com", "password1");
        createUser("user2", "user2@test.com", "password2");
        createUser("user3", "user3@test.com", "password3");
        createUser("user4", "user4@test.com", "password4");
        createUser("user5", "user5@test.com", "password5");

        Pageable pageable = PageRequest.of(0, 2);

        Page<User> page = userRepository.findAllByRole(UserRoles.CUSTOMER, pageable);

        assertEquals(2, page.getContent().size());
        assertEquals(5, page.getTotalElements());
        assertEquals(3, page.getTotalPages());
        assertTrue(page.hasNext());
    }
}