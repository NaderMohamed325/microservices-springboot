package com.neo.customerservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neo.customerservice.config.SecurityConfiguration;
import com.neo.customerservice.dto.user.input.CreateUserInputDto;
import com.neo.customerservice.dto.user.input.LoginUserInputDto;
import com.neo.customerservice.dto.user.output.LoginUserOutputDto;
import com.neo.customerservice.dto.user.output.UserOutputDto;
import com.neo.customerservice.exceptions.UserAlreadyExistsException;
import com.neo.customerservice.filter.JwtAuthFilter;
import com.neo.customerservice.services.auth.AuthService;
import com.neo.customerservice.services.jwt.JwtService;
import com.neo.customerservice.services.user.UserDetailServiceImpl;
import com.neo.customerservice.utils.AuthEntryPointJwt;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(SecurityConfiguration.class)
class AuthControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private AuthService authService;
    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;
    @MockitoBean
    private AuthEntryPointJwt authEntryPointJwt;
    @MockitoBean
    private UserDetailServiceImpl userDetailsService;
    @MockitoBean
    private JwtService jwtService;

    @BeforeEach
    void setUp() throws Exception {
        doAnswer(invocation -> {
            FilterChain chain = invocation.getArgument(2);
            chain.doFilter(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(jwtAuthFilter).doFilter(any(), any(), any());
    }

    @Test
    void register_validInput_returns201() throws Exception {
        CreateUserInputDto inputDto = CreateUserInputDto.builder()
                .email("test@example.com")
                .username("testuser")
                .password("password123")
                .build();

        UserOutputDto outputDto = UserOutputDto.builder()
                .id(1L)
                .email("test@example.com")
                .username("testuser")
                .build();

        when(authService.registerUser(any(CreateUserInputDto.class))).thenReturn(outputDto);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    void register_validInput_returnsJsonContentType() throws Exception {
        CreateUserInputDto inputDto = CreateUserInputDto.builder()
                .email("test@example.com")
                .username("testuser")
                .password("password123")
                .build();

        UserOutputDto outputDto = UserOutputDto.builder()
                .id(1L)
                .email("test@example.com")
                .username("testuser")
                .build();

        when(authService.registerUser(any(CreateUserInputDto.class))).thenReturn(outputDto);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void register_invalidEmail_returns400() throws Exception {
        CreateUserInputDto inputDto = CreateUserInputDto.builder()
                .email("not-an-email")
                .username("testuser")
                .password("password123")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_blankUsername_returns400() throws Exception {
        CreateUserInputDto inputDto = CreateUserInputDto.builder()
                .email("test@example.com")
                .username("")
                .password("password123")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_shortPassword_returns400() throws Exception {
        CreateUserInputDto inputDto = CreateUserInputDto.builder()
                .email("test@example.com")
                .username("testuser")
                .password("short")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_duplicateUser_returns409() throws Exception {
        CreateUserInputDto inputDto = CreateUserInputDto.builder()
                .email("test@example.com")
                .username("testuser")
                .password("password123")
                .build();

        when(authService.registerUser(any(CreateUserInputDto.class)))
                .thenThrow(new UserAlreadyExistsException("User already exists with username: testuser"));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isConflict());
    }

    @Test
    void login_validCredentials_returns200() throws Exception {
        LoginUserInputDto inputDto = LoginUserInputDto.builder()
                .username("testuser")
                .password("password123")
                .build();

        LoginUserOutputDto outputDto = LoginUserOutputDto.builder()
                .accessToken("jwt-token-123")
                .build();

        when(authService.login(any(LoginUserInputDto.class))).thenReturn(outputDto);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("jwt-token-123"));
    }

    @Test
    void login_validCredentials_returnsJsonContentType() throws Exception {
        LoginUserInputDto inputDto = LoginUserInputDto.builder()
                .username("testuser")
                .password("password123")
                .build();

        LoginUserOutputDto outputDto = LoginUserOutputDto.builder()
                .accessToken("jwt-token-123")
                .build();

        when(authService.login(any(LoginUserInputDto.class))).thenReturn(outputDto);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void login_invalidCredentials_returns401() throws Exception {
        LoginUserInputDto inputDto = LoginUserInputDto.builder()
                .username("testuser")
                .password("wrongpassword")
                .build();

        when(authService.login(any(LoginUserInputDto.class)))
                .thenThrow(new RuntimeException("Authentication failed"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_blankUsername_returns400() throws Exception {
        LoginUserInputDto inputDto = LoginUserInputDto.builder()
                .username("")
                .password("password123")
                .build();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_blankPassword_returns400() throws Exception {
        LoginUserInputDto inputDto = LoginUserInputDto.builder()
                .username("testuser")
                .password("")
                .build();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isBadRequest());
    }
}
