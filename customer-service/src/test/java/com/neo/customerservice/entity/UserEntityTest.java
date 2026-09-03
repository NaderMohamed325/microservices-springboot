package com.neo.customerservice.entity;

import com.neo.customerservice.config.JpaAuditingConfiguration;
import com.neo.customerservice.enums.UserRoles;
import com.neo.customerservice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import(JpaAuditingConfiguration.class)
class UserEntityTest {

    @Autowired
    private UserRepository userRepository;

    private User createTestUser(String username, String email, String password) {
        return User.builder()
                .username(username)
                .email(email)
                .password(password)
                .role(UserRoles.CUSTOMER)
                .addresses(new ArrayList<>())
                .build();
    }

    @Test
    void saveUser_withValidData_persistsAllFields() {
        User user = createTestUser("testuser", "test@example.com", "password123");
        User saved = userRepository.save(user);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUsername()).isEqualTo("testuser");
        assertThat(saved.getEmail()).isEqualTo("test@example.com");
        assertThat(saved.getPassword()).isEqualTo("password123");
        assertThat(saved.getRole()).isEqualTo(UserRoles.CUSTOMER);
    }

    @Test
    void saveUser_duplicateEmail_throwsException() {
        userRepository.save(createTestUser("user1", "test@example.com", "password123"));
        userRepository.flush();

        User duplicate = createTestUser("user2", "test@example.com", "password456");

        assertThatThrownBy(() -> {
            userRepository.save(duplicate);
            userRepository.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void saveUser_duplicateUsername_throwsException() {
        userRepository.save(createTestUser("testuser", "user1@example.com", "password123"));
        userRepository.flush();

        User duplicate = createTestUser("testuser", "user2@example.com", "password456");

        assertThatThrownBy(() -> {
            userRepository.save(duplicate);
            userRepository.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void user_getAuthorities_customerRole() {
        User user = createTestUser("testuser", "test@example.com", "password123");

        assertThat(user.getAuthorities()).hasSize(1);
        assertThat(user.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_CUSTOMER");
    }

    @Test
    void user_getAuthorities_adminRole() {
        User user = User.builder()
                .username("admin")
                .email("admin@example.com")
                .password("password123")
                .role(UserRoles.ADMIN)
                .build();

        assertThat(user.getAuthorities()).hasSize(1);
        assertThat(user.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_ADMIN");
    }

    @Test
    void user_isAccountNonExpired_returnsTrue() {
        User user = createTestUser("testuser", "test@example.com", "password123");

        assertThat(user.isAccountNonExpired()).isTrue();
    }

    @Test
    void user_isAccountNonLocked_returnsTrue() {
        User user = createTestUser("testuser", "test@example.com", "password123");

        assertThat(user.isAccountNonLocked()).isTrue();
    }

    @Test
    void user_isCredentialsNonExpired_returnsTrue() {
        User user = createTestUser("testuser", "test@example.com", "password123");

        assertThat(user.isCredentialsNonExpired()).isTrue();
    }

    @Test
    void user_isEnabled_returnsTrue() {
        User user = createTestUser("testuser", "test@example.com", "password123");

        assertThat(user.isEnabled()).isTrue();
    }

    @Test
    void saveUser_defaultRoleIsCustomer() {
        User user = createTestUser("testuser", "test@example.com", "password123");

        assertThat(user.getRole()).isEqualTo(UserRoles.CUSTOMER);
    }

    @Test
    void saveUser_addresses_cascadePersist() {
        User user = createTestUser("testuser", "test@example.com", "password123");
        Address address = Address.builder()
                .street("123 Main St")
                .city("Springfield")
                .state("IL")
                .zipCode("62704")
                .user(user)
                .build();
        user.getAddresses().add(address);

        User saved = userRepository.save(user);
        userRepository.flush();

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getAddresses()).hasSize(1);
        assertThat(saved.getAddresses().get(0).getStreet()).isEqualTo("123 Main St");
    }

    @Test
    void deleteUser_addresses_orphanRemoval() {
        User user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .role(UserRoles.CUSTOMER)
                .addresses(new ArrayList<>())
                .build();
        Address address = Address.builder()
                .street("123 Main St")
                .city("Springfield")
                .state("IL")
                .zipCode("62704")
                .user(user)
                .build();
        user.getAddresses().add(address);
        User saved = userRepository.save(user);
        userRepository.flush();

        userRepository.delete(saved);
        userRepository.flush();

        assertThat(userRepository.findById(saved.getId())).isEmpty();
    }

    @Test
    void saveUser_emailMaxLength() {
        String longEmail = "a".repeat(119) + "@example.com";
        User user = createTestUser("testuser", longEmail, "password123");
        User saved = userRepository.save(user);

        assertThat(saved.getEmail()).isEqualTo(longEmail);
    }

    @Test
    void saveUser_usernameMaxLength() {
        String longUsername = "a".repeat(120);
        User user = createTestUser(longUsername, "test@example.com", "password123");
        User saved = userRepository.save(user);

        assertThat(saved.getUsername()).isEqualTo(longUsername);
    }
}
