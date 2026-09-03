package com.neo.customerservice.services.user;

import com.neo.customerservice.dto.user.input.CreateUserInputDto;
import com.neo.customerservice.dto.user.output.UserOutputDto;
import com.neo.customerservice.entity.User;
import com.neo.customerservice.enums.UserRoles;
import com.neo.customerservice.exceptions.UserAlreadyExistsException;
import com.neo.customerservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserOutputDto createUser(@NonNull CreateUserInputDto createUserDto) {

        boolean existingUser = userRepository.findByUsername(createUserDto.getUsername()).isPresent();
        if (existingUser) {
            throw new UserAlreadyExistsException("User already exists with username: " + createUserDto.getUsername());
        }

        User user = User.builder()
                .email(createUserDto.getEmail())
                .username(createUserDto.getUsername())
                .password(passwordEncoder.encode(createUserDto.getPassword()))
                .build();

        userRepository.save(user);
        log.info("User created with username: {}", user.getUsername());

        return toOutputDto(user);
    }

    @Override
    public UserOutputDto getUserByUsername(@NonNull String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.error("User not found with username: {}", username);
                    return new UserAlreadyExistsException("User not found with username: " + username);
                });

        return toOutputDto(user);
    }

    @Override
    public Page<UserOutputDto> getCustomersPaginated(int page, int size, String sortBy, String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return userRepository.findAllByRole(UserRoles.CUSTOMER, pageable).map(this::toOutputDto);
    }

    private UserOutputDto toOutputDto(User user) {
        return UserOutputDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .build();
    }

}
