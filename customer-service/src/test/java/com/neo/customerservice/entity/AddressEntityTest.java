package com.neo.customerservice.entity;

import com.neo.customerservice.config.JpaAuditingConfiguration;
import com.neo.customerservice.enums.UserRoles;
import com.neo.customerservice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaAuditingConfiguration.class)
class AddressEntityTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void saveAddress_withValidUser_persistsCorrectly() {
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

        assertThat(saved.getAddresses()).hasSize(1);
        Address savedAddress = saved.getAddresses().get(0);
        assertThat(savedAddress.getStreet()).isEqualTo("123 Main St");
        assertThat(savedAddress.getCity()).isEqualTo("Springfield");
        assertThat(savedAddress.getState()).isEqualTo("IL");
        assertThat(savedAddress.getZipCode()).isEqualTo("62704");
    }

    @Test
    void saveAddress_multipleAddresses_persistsAll() {
        User user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .role(UserRoles.CUSTOMER)
                .addresses(new ArrayList<>())
                .build();

        Address address1 = Address.builder()
                .street("123 Main St")
                .city("Springfield")
                .state("IL")
                .zipCode("62704")
                .user(user)
                .build();
        Address address2 = Address.builder()
                .street("456 Oak Ave")
                .city("Chicago")
                .state("IL")
                .zipCode("60601")
                .user(user)
                .build();
        user.getAddresses().add(address1);
        user.getAddresses().add(address2);

        User saved = userRepository.save(user);
        userRepository.flush();

        assertThat(saved.getAddresses()).hasSize(2);
    }

    @Test
    void deleteAddress_orphanRemoval() {
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

        saved.getAddresses().clear();
        userRepository.save(saved);
        userRepository.flush();

        User found = userRepository.findById(saved.getId()).orElseThrow();
        assertThat(found.getAddresses()).isEmpty();
    }
}
