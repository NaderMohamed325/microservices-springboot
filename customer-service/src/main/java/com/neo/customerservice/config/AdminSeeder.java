package com.neo.customerservice.config;

import com.neo.customerservice.entity.User;
import com.neo.customerservice.enums.UserRoles;
import com.neo.customerservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
class AdminSeeder implements CommandLineRunner {


    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        User admin = User.builder()
                .username("admin")
                .email("admin@admin.com")
                .password(passwordEncoder.encode("admin"))
                .role(UserRoles.ADMIN)
                .build();
        userRepository.save(admin);
        log.info("Admin user created: {}", admin.getUsername());
    }
}
