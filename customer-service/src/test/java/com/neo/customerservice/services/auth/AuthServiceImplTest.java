package com.neo.customerservice.services.auth;

import com.neo.customerservice.dto.user.input.CreateUserInputDto;
import com.neo.customerservice.dto.user.input.LoginUserInputDto;
import com.neo.customerservice.dto.user.output.LoginUserOutputDto;
import com.neo.customerservice.dto.user.output.UserOutputDto;
import com.neo.customerservice.entity.User;
import com.neo.customerservice.enums.UserRoles;
import com.neo.customerservice.services.jwt.JwtService;
import com.neo.customerservice.services.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthServiceImpl authService;

    private LoginUserInputDto loginUserInputDto;
    private CreateUserInputDto createUserInputDto;
    private User testUser;
    private UserOutputDto userOutputDto;

    @BeforeEach
    void setUp() {
        loginUserInputDto = LoginUserInputDto.builder()
                .username("testuser")
                .password("password123")
                .build();

        createUserInputDto = CreateUserInputDto.builder()
                .email("test@example.com")
                .username("testuser")
                .password("password123")
                .build();

        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .role(UserRoles.CUSTOMER)
                .addresses(new ArrayList<>())
                .build();

        userOutputDto = UserOutputDto.builder()
                .id(1L)
                .email("test@example.com")
                .username("testuser")
                .build();
    }

    @Test
    void login_validCredentials_returnsLoginUserOutputDto() {
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(testUser);
        when(jwtService.generateToken(testUser)).thenReturn("jwt-token-123");

        LoginUserOutputDto result = authService.login(loginUserInputDto);

        assertThat(result).isNotNull();
        assertThat(result.getAccessToken()).isEqualTo("jwt-token-123");
        verify(userDetailsService).loadUserByUsername("testuser");
        verify(jwtService).generateToken(testUser);
    }

    @Test
    void login_validCredentials_loadsUserDetails() {
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(testUser);
        when(jwtService.generateToken(testUser)).thenReturn("jwt-token-123");

        authService.login(loginUserInputDto);

        verify(userDetailsService).loadUserByUsername("testuser");
    }

    @Test
    void login_validCredentials_generatesToken() {
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(testUser);
        when(jwtService.generateToken(testUser)).thenReturn("jwt-token-123");

        authService.login(loginUserInputDto);

        verify(jwtService).generateToken(testUser);
    }

    @Test
    void registerUser_validInput_returnsUserOutputDto() {
        when(userService.createUser(any(CreateUserInputDto.class))).thenReturn(userOutputDto);

        UserOutputDto result = authService.registerUser(createUserInputDto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        verify(userService).createUser(createUserInputDto);
    }

    @Test
    void registerUser_validInput_delegatesToUserService() {
        when(userService.createUser(any(CreateUserInputDto.class))).thenReturn(userOutputDto);

        authService.registerUser(createUserInputDto);

        verify(userService).createUser(createUserInputDto);
    }
}
