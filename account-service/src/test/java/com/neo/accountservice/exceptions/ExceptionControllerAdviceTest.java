package com.neo.accountservice.exceptions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ExceptionControllerAdviceTest {

    private ExceptionControllerAdvice exceptionControllerAdvice;

    @BeforeEach
    void setUp() {
        exceptionControllerAdvice = new ExceptionControllerAdvice();
    }

    @Test
    void handleAccessDeniedException_returnsForbiddenStatus() {
        AccessDeniedException ex = new AccessDeniedException("Access denied");

        ResponseEntity<Map<String, Object>> response = exceptionControllerAdvice.handleAccessDeniedException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(403);
        assertThat(response.getBody().get("error")).isEqualTo("Forbidden");
        assertThat(response.getBody().get("message")).isEqualTo("Access denied");
    }

    @Test
    void handleValidationException_returnsBadRequestStatus() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("dto", "accountType", "must not be null");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<Map<String, Object>> response = exceptionControllerAdvice.handleValidationException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(400);
        assertThat(response.getBody().get("error")).isEqualTo("Bad Request");
        assertThat(response.getBody().get("message")).isEqualTo("Validation failed");
        assertThat(response.getBody().get("details")).isNotNull();
    }

    @Test
    void handleAccountNotFoundException_returnsNotFoundStatus() {
        AccountNotFoundException ex = new AccountNotFoundException("Account not found with id: 999");

        ResponseEntity<Map<String, Object>> response = exceptionControllerAdvice.handleAccountNotFoundException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(404);
        assertThat(response.getBody().get("error")).isEqualTo("Not Found");
        assertThat(response.getBody().get("message")).isEqualTo("Account not found with id: 999");
    }

    @Test
    void handleCustomerNotFoundException_returnsNotFoundStatus() {
        CustomerNotFoundException ex = new CustomerNotFoundException("CustomerStatus not found for customerId: 1");

        ResponseEntity<Map<String, Object>> response = exceptionControllerAdvice.handleCustomerNotFoundException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("message")).isEqualTo("CustomerStatus not found for customerId: 1");
    }

    @Test
    void handleCustomerInactiveException_returnsBadRequestStatus() {
        CustomerInactiveException ex = new CustomerInactiveException("Cannot create account for inactive customerId: 1");

        ResponseEntity<Map<String, Object>> response = exceptionControllerAdvice.handleCustomerInactiveException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("message")).isEqualTo("Cannot create account for inactive customerId: 1");
    }

    @Test
    void handleSalaryAccountAlreadyExistsException_returnsConflictStatus() {
        SalaryAccountAlreadyExistsException ex = new SalaryAccountAlreadyExistsException("Customer already has a SALARY account: 1");

        ResponseEntity<Map<String, Object>> response = exceptionControllerAdvice.handleSalaryAccountAlreadyExistsException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("message")).isEqualTo("Customer already has a SALARY account: 1");
    }

    @Test
    void handleMaxAccountsReachedException_returnsBadRequestStatus() {
        MaxAccountsReachedException ex = new MaxAccountsReachedException("Maximum number of accounts reached for customerId: 1");

        ResponseEntity<Map<String, Object>> response = exceptionControllerAdvice.handleMaxAccountsReachedException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("message")).isEqualTo("Maximum number of accounts reached for customerId: 1");
    }

    @Test
    void handleAccountSuspendedException_returnsBadRequestStatus() {
        AccountSuspendedException ex = new AccountSuspendedException("Account is suspended: 1001");

        ResponseEntity<Map<String, Object>> response = exceptionControllerAdvice.handleAccountSuspendedException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("message")).isEqualTo("Account is suspended: 1001");
    }

    @Test
    void handleGenericException_returnsInternalServerError() {
        Exception ex = new Exception("Unexpected error");

        ResponseEntity<Map<String, Object>> response = exceptionControllerAdvice.handleGenericException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(500);
        assertThat(response.getBody().get("error")).isEqualTo("Internal Server Error");
        assertThat(response.getBody().get("message")).isEqualTo("An unexpected error occurred");
    }

    @Test
    void handleAccessDeniedException_responseBodyContainsTimestamp() {
        AccessDeniedException ex = new AccessDeniedException("Access denied");

        ResponseEntity<Map<String, Object>> response = exceptionControllerAdvice.handleAccessDeniedException(ex);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("timestamp")).isNotNull();
        assertThat(response.getBody().get("timestamp").toString()).isNotEmpty();
    }
}
