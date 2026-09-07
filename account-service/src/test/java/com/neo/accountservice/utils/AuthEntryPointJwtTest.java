package com.neo.accountservice.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AuthEntryPointJwtTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private AuthEntryPointJwt authEntryPointJwt;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private BadCredentialsException authException;

    @BeforeEach
    void setUp() {
        authEntryPointJwt = new AuthEntryPointJwt();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        authException = new BadCredentialsException("Invalid credentials");
    }

    @Test
    void commence_unauthorizedAccess_returns401Status() throws IOException {
        authEntryPointJwt.commence(request, response, authException);

        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    void commence_unauthorizedAccess_returnsJsonContentType() throws IOException {
        authEntryPointJwt.commence(request, response, authException);

        assertThat(response.getContentType()).isEqualTo("application/json");
    }

    @Test
    void commence_unauthorizedAccess_returnsCorrectResponseBody() throws IOException {
        authEntryPointJwt.commence(request, response, authException);

        String responseBody = response.getContentAsString();
        Map<String, Object> body = objectMapper.readValue(responseBody, Map.class);

        assertThat(body).isNotNull();
        assertThat(body.get("status")).isEqualTo(401);
        assertThat(body.get("error")).isEqualTo("Unauthorized");
        assertThat(body.get("message")).isEqualTo("Invalid credentials");
        assertThat(body.get("timestamp")).isNotNull();
        assertThat(body.get("timestamp").toString()).isNotEmpty();
    }

    @Test
    void commence_withDifferentException_returnsExceptionMessage() throws IOException {
        BadCredentialsException differentException = new BadCredentialsException("Wrong password");

        authEntryPointJwt.commence(request, response, differentException);

        String responseBody = response.getContentAsString();
        Map<String, Object> body = objectMapper.readValue(responseBody, Map.class);

        assertThat(body.get("message")).isEqualTo("Wrong password");
    }
}
