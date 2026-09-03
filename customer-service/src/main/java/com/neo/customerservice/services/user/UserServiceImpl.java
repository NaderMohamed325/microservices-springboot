package com.neo.customerservice.services.user;

import com.neo.customerservice.dto.user.input.CreateUserInputDto;
import com.neo.customerservice.dto.user.input.UpdateUserInputDto;
import com.neo.customerservice.dto.user.output.UserOutputDto;
import com.neo.customerservice.entity.User;
import com.neo.customerservice.enums.UserRoles;
import com.neo.customerservice.exceptions.UserAlreadyExistsException;
import com.neo.customerservice.exceptions.UserNotFoundException;
import com.neo.customerservice.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

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
                    return new UserNotFoundException("User not found with username: " + username);
                });

        return toOutputDto(user);
    }

    @Override
    public Page<UserOutputDto> getCustomersPaginated(int page, int size, String sortBy, String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return userRepository.findAllByRole(UserRoles.CUSTOMER, pageable).map(this::toOutputDto);
    }

    @Override
    @Transactional
    public UserOutputDto updateUser(Long userId, UpdateUserInputDto updateUserInputDto) {

        User actor = this.getUserById(userId);

        boolean isAdmin = actor.getRole() == UserRoles.ADMIN;

        boolean isSelfUpdate = actor.getId().equals(updateUserInputDto.getId());

        if (!isAdmin && !isSelfUpdate) {
            log.error("User with id: {} is not authorized to update user with id: {}", actor.getId(), updateUserInputDto.getId());
            throw new AccessDeniedException("You are not authorized to update this user");
        }

        User user = this.getUserById(updateUserInputDto.getId());

        if (updateUserInputDto.getEmail() != null) {
            user.setEmail(updateUserInputDto.getEmail());
        }
        if (updateUserInputDto.getUsername() != null) {
            user.setUsername(updateUserInputDto.getUsername());
        }
        if (updateUserInputDto.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(updateUserInputDto.getPassword()));
        }

        userRepository.save(user);
        log.info("User updated with id: {}", user.getId());

        return toOutputDto(user);
    }

    @Override
    public User getUserById(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            log.error("User not found with id: {}", userId);
            throw new UserNotFoundException("User not found with id: " + userId);
        }
        return userOptional.get();
    }

    @Override
    public UserOutputDto getUserByIdOutputDto(Long userId) {
        return toOutputDto(this.getUserById(userId));
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        User user = this.getUserById(userId);

        userRepository.deleteUserById(user.getId());
        log.info("User deleted with id: {}", user.getId());
    }


    private UserOutputDto toOutputDto(User user) {
        return UserOutputDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .build();
    }

}
