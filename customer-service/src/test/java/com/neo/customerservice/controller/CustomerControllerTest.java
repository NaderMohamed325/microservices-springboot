package com.neo.customerservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neo.customerservice.config.SecurityConfiguration;
import com.neo.customerservice.dto.user.input.UpdateUserInputDto;
import com.neo.customerservice.dto.user.output.UserOutputDto;
import com.neo.customerservice.entity.User;
import com.neo.customerservice.enums.UserRoles;
import com.neo.customerservice.exceptions.UserNotFoundException;
import com.neo.customerservice.filter.JwtAuthFilter;
import com.neo.customerservice.services.jwt.JwtService;
import com.neo.customerservice.services.user.UserDetailServiceImpl;
import com.neo.customerservice.services.user.UserService;
import com.neo.customerservice.utils.AuthEntryPointJwt;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CustomerController.class)
@Import(SecurityConfiguration.class)
class CustomerControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private UserService userService;
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

        doAnswer(invocation -> {
            HttpServletResponse response = invocation.getArgument(1);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Unauthorized\",\"status\":401}");
            return null;
        }).when(authEntryPointJwt).commence(any(), any(), any());
    }

    private UserOutputDto createUserOutputDto(Long id, String email, String username) {
        return UserOutputDto.builder()
                .id(id)
                .email(email)
                .username(username)
                .build();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllCustomers_adminRole_returns200() throws Exception {
        UserOutputDto user = createUserOutputDto(1L, "test@example.com", "testuser");
        Page<UserOutputDto> page = new PageImpl<>(List.of(user));

        when(userService.getCustomersPaginated(0, 10, "id", "asc")).thenReturn(page);

        mockMvc.perform(get("/api/v1/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].email").value("test@example.com"))
                .andExpect(jsonPath("$.content[0].username").value("testuser"));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void getAllCustomers_customerRole_returns200() throws Exception {
        UserOutputDto user = createUserOutputDto(1L, "test@example.com", "testuser");
        Page<UserOutputDto> page = new PageImpl<>(List.of(user));

        when(userService.getCustomersPaginated(0, 10, "id", "asc")).thenReturn(page);

        mockMvc.perform(get("/api/v1/customers"))
                .andExpect(status().isOk());
    }

    @Test
    void getAllCustomers_noAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/customers"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllCustomers_withPagination_passesParams() throws Exception {
        Page<UserOutputDto> page = new PageImpl<>(List.of());

        when(userService.getCustomersPaginated(2, 5, "username", "desc")).thenReturn(page);

        mockMvc.perform(get("/api/v1/customers")
                        .param("page", "2")
                        .param("size", "5")
                        .param("sortBy", "username")
                        .param("sortDir", "desc"))
                .andExpect(status().isOk());

        verify(userService).getCustomersPaginated(2, 5, "username", "desc");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getCustomerById_adminRole_returns200() throws Exception {
        UserOutputDto user = createUserOutputDto(1L, "test@example.com", "testuser");

        when(userService.getUserByIdOutputDto(1L)).thenReturn(user);

        mockMvc.perform(get("/api/v1/customers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void getCustomerById_customerRole_returns200() throws Exception {
        UserOutputDto user = createUserOutputDto(1L, "test@example.com", "testuser");

        when(userService.getUserByIdOutputDto(1L)).thenReturn(user);

        mockMvc.perform(get("/api/v1/customers/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getCustomerById_notFound_returns404() throws Exception {
        when(userService.getUserByIdOutputDto(999L))
                .thenThrow(new UserNotFoundException("User not found with id: 999"));

        mockMvc.perform(get("/api/v1/customers/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteCustomer_adminRole_returns204() throws Exception {
        mockMvc.perform(delete("/api/v1/customers/1"))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(1L);
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void deleteCustomer_customerRole_returns403() throws Exception {
        mockMvc.perform(delete("/api/v1/customers/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteCustomer_noAuth_returns401() throws Exception {
        mockMvc.perform(delete("/api/v1/customers/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteCustomer_notFound_returns404() throws Exception {
        doThrow(new UserNotFoundException("User not found with id: 999"))
                .when(userService).deleteUser(999L);

        mockMvc.perform(delete("/api/v1/customers/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateCustomer_adminRole_returns200() throws Exception {
        User actor = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .role(UserRoles.ADMIN)
                .addresses(new ArrayList<>())
                .build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(actor, null, actor.getAuthorities()));

        UpdateUserInputDto inputDto = UpdateUserInputDto.builder()
                .id(1L)
                .email("updated@example.com")
                .username("updateduser")
                .password("newpassword123")
                .build();

        UserOutputDto outputDto = createUserOutputDto(1L, "updated@example.com", "updateduser");

        when(userService.updateUser(eq(1L), any(UpdateUserInputDto.class))).thenReturn(outputDto);

        mockMvc.perform(put("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("updated@example.com"))
                .andExpect(jsonPath("$.username").value("updateduser"));
    }

    @Test
    void updateCustomer_invalidInput_returns400() throws Exception {
        User actor = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .role(UserRoles.ADMIN)
                .addresses(new ArrayList<>())
                .build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(actor, null, actor.getAuthorities()));

        UpdateUserInputDto inputDto = UpdateUserInputDto.builder()
                .id(1L)
                .email("not-an-email")
                .username("")
                .password("short")
                .build();

        mockMvc.perform(put("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateCustomer_notFound_returns404() throws Exception {
        User actor = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .role(UserRoles.ADMIN)
                .addresses(new ArrayList<>())
                .build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(actor, null, actor.getAuthorities()));

        UpdateUserInputDto inputDto = UpdateUserInputDto.builder()
                .id(999L)
                .email("updated@example.com")
                .username("updateduser")
                .password("newpassword123")
                .build();

        when(userService.updateUser(eq(1L), any(UpdateUserInputDto.class)))
                .thenThrow(new UserNotFoundException("User not found with id: 999"));

        mockMvc.perform(put("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isNotFound());
    }
}
