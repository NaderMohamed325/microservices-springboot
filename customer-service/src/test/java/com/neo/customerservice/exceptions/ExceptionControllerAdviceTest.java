package com.neo.customerservice.exceptions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ExceptionControllerAdviceTest {

    private ExceptionControllerAdvice exceptionControllerAdvice;

    @BeforeEach
    void setUp() {
        exceptionControllerAdvice = new ExceptionControllerAdvice();
    }

    @Test
    void handleUserNotFoundException_returnsNotFoundStatus() {
        UserNotFoundException ex = new UserNotFoundException("User not found with id: 1");

        ResponseEntity<Map<String, Object>> response = exceptionControllerAdvice.handleUserNotFoundException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(404);
        assertThat(response.getBody().get("error")).isEqualTo("Not Found");
        assertThat(response.getBody().get("message")).isEqualTo("User not found with id: 1");
    }

    @Test
    void handleUserAlreadyExistsException_returnsConflictStatus() {
        UserAlreadyExistsException ex = new UserAlreadyExistsException("User already exists with username: testuser");

        ResponseEntity<Map<String, Object>> response = exceptionControllerAdvice.handleUserAlreadyExistsException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(409);
        assertThat(response.getBody().get("error")).isEqualTo("Conflict");
        assertThat(response.getBody().get("message")).isEqualTo("User already exists with username: testuser");
    }

    @Test
    void handleBadCredentialsException_returnsUnauthorizedStatus() {
        BadCredentialsException ex = new BadCredentialsException("Invalid credentials");

        ResponseEntity<Map<String, Object>> response = exceptionControllerAdvice.handleBadCredentialsException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(401);
        assertThat(response.getBody().get("error")).isEqualTo("Unauthorized");
        assertThat(response.getBody().get("message")).isEqualTo("Invalid username or password");
    }

    @Test
    void handleGenericException_returnsInternalServerErrorStatus() {
        Exception ex = new RuntimeException("Something went wrong");

        ResponseEntity<Map<String, Object>> response = exceptionControllerAdvice.handleGenericException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(500);
        assertThat(response.getBody().get("error")).isEqualTo("Internal Server Error");
        assertThat(response.getBody().get("message")).isEqualTo("An unexpected error occurred");
    }

    @Test
    void handleUserNotFoundException_responseBodyContainsTimestamp() {
        UserNotFoundException ex = new UserNotFoundException("User not found");

        ResponseEntity<Map<String, Object>> response = exceptionControllerAdvice.handleUserNotFoundException(ex);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("timestamp")).isNotNull();
        assertThat(response.getBody().get("timestamp").toString()).isNotEmpty();
    }
}
