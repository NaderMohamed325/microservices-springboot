package com.neo.customerservice.services;

import com.neo.customerservice.dto.user.input.CreateUserInputDto;
import com.neo.customerservice.dto.user.input.UpdateUserInputDto;
import com.neo.customerservice.dto.user.output.UserOutputDto;
import com.neo.customerservice.entity.User;
import com.neo.customerservice.enums.UserRoles;
import com.neo.customerservice.exceptions.UserAlreadyExistsException;
import com.neo.customerservice.exceptions.UserNotFoundException;
import com.neo.customerservice.repository.UserRepository;
import com.neo.customerservice.services.event.EventService;
import com.neo.customerservice.services.user.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EventService eventService;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private CreateUserInputDto createUserInputDto;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .username("testuser")
                .password("encodedPassword")
                .role(UserRoles.CUSTOMER)
                .build();

        createUserInputDto = CreateUserInputDto.builder()
                .email("new@example.com")
                .username("newuser")
                .password("password123")
                .build();

        UpdateUserInputDto updateUserInputDto = UpdateUserInputDto.builder()
                .id(1L)
                .email("updated@example.com")
                .username("updateduser")
                .password("newpassword123")
                .build();
    }

    @Test
    void createUser_newUser_returnsUserOutputDto() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        UserOutputDto result = userService.createUser(createUserInputDto);

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("new@example.com");
        assertThat(result.getUsername()).isEqualTo("newuser");
        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode("password123");
    }

    @Test
    void createUser_existingUsername_throwsUserAlreadyExistsException() {
        when(userRepository.findByUsername("newuser")).thenReturn(Optional.of(testUser));

        assertThatThrownBy(() -> userService.createUser(createUserInputDto))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("User already exists with username: newuser");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getUserByUsername_userExists_returnsUserOutputDto() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        UserOutputDto result = userService.getUserByUsername("testuser");

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUsername()).isEqualTo("testuser");
    }

    @Test
    void getUserByUsername_userNotFound_throwsUserNotFoundException() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserByUsername("nonexistent"))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User not found with username: nonexistent");
    }

    @Test
    void getUserById_userExists_returnsUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        User result = userService.getUserById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUsername()).isEqualTo("testuser");
    }

    @Test
    void getUserById_userNotFound_throwsUserNotFoundException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(999L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User not found with id: 999");
    }

    @Test
    void getUserByIdOutputDto_userExists_returnsUserOutputDto() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        UserOutputDto result = userService.getUserByIdOutputDto(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUsername()).isEqualTo("testuser");
    }

    @Test
    void getCustomersPaginated_validPage_returnsPageOfUsers() {
        User customer = User.builder()
                .id(2L)
                .email("customer@example.com")
                .username("customer")
                .password("encodedPassword")
                .role(UserRoles.CUSTOMER)
                .build();

        Page<User> userPage = new PageImpl<>(List.of(customer), PageRequest.of(0, 10, Sort.by("id").ascending()), 1);
        when(userRepository.findAllByRole(eq(UserRoles.CUSTOMER), any(PageRequest.class))).thenReturn(userPage);

        Page<UserOutputDto> result = userService.getCustomersPaginated(0, 10, "id", "asc");

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getUsername()).isEqualTo("customer");
        verify(userRepository).findAllByRole(eq(UserRoles.CUSTOMER), any(PageRequest.class));
    }

    @Test
    void updateUser_adminUpdatingAnotherUser_returnsUpdatedUser() {
        User admin = User.builder()
                .id(1L)
                .email("admin@example.com")
                .username("admin")
                .password("encodedPassword")
                .role(UserRoles.ADMIN)
                .build();

        User targetUser = User.builder()
                .id(2L)
                .email("target@example.com")
                .username("targetuser")
                .password("encodedPassword")
                .role(UserRoles.CUSTOMER)
                .build();

        UpdateUserInputDto adminUpdateDto = UpdateUserInputDto.builder()
                .id(2L)
                .email("updated@example.com")
                .username("updateduser")
                .password("newpassword123")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(userRepository.findById(2L)).thenReturn(Optional.of(targetUser));
        when(passwordEncoder.encode("newpassword123")).thenReturn("newEncodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(targetUser);

        UserOutputDto result = userService.updateUser(1L, adminUpdateDto);

        assertThat(result).isNotNull();
        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode("newpassword123");
    }

    @Test
    void updateUser_userUpdatingSelf_returnsUpdatedUser() {
        UpdateUserInputDto selfUpdateDto = UpdateUserInputDto.builder()
                .id(1L)
                .email("updated@example.com")
                .username("updateduser")
                .password("newpassword123")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode("newpassword123")).thenReturn("newEncodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserOutputDto result = userService.updateUser(1L, selfUpdateDto);

        assertThat(result).isNotNull();
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUser_userUpdatingAnotherUser_throwsAccessDeniedException() {
        User regularUser = User.builder()
                .id(1L)
                .email("user@example.com")
                .username("user")
                .password("encodedPassword")
                .role(UserRoles.CUSTOMER)
                .build();

        User anotherUser = User.builder()
                .id(2L)
                .email("another@example.com")
                .username("anotheruser")
                .password("encodedPassword")
                .role(UserRoles.CUSTOMER)
                .build();

        UpdateUserInputDto updateDto = UpdateUserInputDto.builder()
                .id(2L)
                .email("updated@example.com")
                .username("updateduser")
                .password("newpassword123")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(regularUser));

        assertThatThrownBy(() -> userService.updateUser(1L, updateDto))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("You are not authorized to update this user");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUser_targetUserNotFound_throwsUserNotFoundException() {
        User admin = User.builder()
                .id(1L)
                .email("admin@example.com")
                .username("admin")
                .password("encodedPassword")
                .role(UserRoles.ADMIN)
                .build();

        UpdateUserInputDto updateDto = UpdateUserInputDto.builder()
                .id(999L)
                .email("updated@example.com")
                .username("updateduser")
                .password("newpassword123")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser(1L, updateDto))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User not found with id: 999");
    }

    @Test
    void updateUser_nullPassword_passwordNotUpdated() {
        UpdateUserInputDto updateDtoNoPassword = UpdateUserInputDto.builder()
                .id(1L)
                .email("updated@example.com")
                .username("updateduser")
                .password(null)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserOutputDto result = userService.updateUser(1L, updateDtoNoPassword);

        assertThat(result).isNotNull();
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void deleteUser_userExists_deletesSuccessfully() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        userService.deleteUser(1L);

        verify(userRepository).deleteUserById(1L);
    }

    @Test
    void deleteUser_userNotFound_throwsUserNotFoundException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteUser(999L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User not found with id: 999");

        verify(userRepository, never()).deleteUserById(any());
    }
}
