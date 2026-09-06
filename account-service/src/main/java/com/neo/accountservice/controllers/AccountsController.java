package com.neo.accountservice.controllers;

import com.neo.accountservice.dto.account.input.CreateAccountInputDto;
import com.neo.accountservice.dto.account.input.UpdateAccountInputDto;
import com.neo.accountservice.entity.Account;
import com.neo.accountservice.services.account.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Accounts", description = "Account management operations")
public class AccountsController {

    private final AccountService accountService;

    @PostMapping()
    @PreAuthorize("hasRole('ADMIN') or hasRole('CUSTOMER')")
    @Operation(summary = "Create account", description = "Create a new account for a customer")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Account created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    public ResponseEntity<Account> createAccount(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateAccountInputDto dto,
            Authentication authentication) {

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> Objects.equals(a.getAuthority(), "ROLE_ADMIN"));

        Long targetCustomerId = isAdmin
                ? requireNonNull(dto.getCustomerId(), "customerId is required for ADMIN-created accounts")
                : jwt.getClaim("userId");

        Account account = accountService.createAccount(targetCustomerId, dto.getAccountType());
        log.info("Account created with id: {} for customerId: {}", account.getId(), targetCustomerId);
        return ResponseEntity.status(201).body(account);
    }

    @GetMapping()
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all accounts", description = "Retrieve paginated and sorted list of accounts (ADMIN only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Accounts retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - ADMIN role required")
    })
    public Page<Account> getAllAccounts(
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Field to sort by", example = "id")
            @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Sort direction (asc or desc)", example = "asc")
            @RequestParam(defaultValue = "asc") String sortDir) {
        log.info("Fetching all accounts - page: {}, size: {}, sortBy: {}, sortDir: {}", page, size, sortBy, sortDir);
        return accountService.getAllAccounts(page, size, sortBy, sortDir);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CUSTOMER')")
    @Operation(summary = "Get account by ID", description = "Retrieve a specific account by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Account found and returned"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    public ResponseEntity<Account> getAccountById(
            @Parameter(description = "Account ID", required = true, example = "1000000")
            @PathVariable("id") Long accountId) {
        log.info("Fetching account with id: {}", accountId);
        Account account = accountService.getAccountById(accountId);
        return ResponseEntity.ok(account);
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get accounts by customer ID", description = "Retrieve paginated accounts for a specific customer (ADMIN only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Accounts retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - ADMIN role required")
    })
    public Page<Account> getAccountsByCustomerId(
            @Parameter(description = "Customer ID", required = true, example = "1000000")
            @PathVariable("customerId") Long customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        log.info("Fetching accounts for customerId: {}", customerId);
        return accountService.getAccountsByCustomerId(customerId, page, size, sortBy, sortDir);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update account", description = "Update account details (ADMIN only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Account updated successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - ADMIN role required"),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    public ResponseEntity<Account> updateAccount(
            @Parameter(description = "Account ID", required = true, example = "1000000")
            @PathVariable("id") Long accountId,
            @Valid @RequestBody UpdateAccountInputDto dto) {
        log.info("Updating account with id: {}", accountId);
        Account account = accountService.updateAccount(accountId, dto);
        return ResponseEntity.ok(account);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete account", description = "Delete an account (ADMIN only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Account deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - ADMIN role required"),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    public ResponseEntity<Void> deleteAccount(
            @Parameter(description = "Account ID", required = true, example = "1000000")
            @PathVariable("id") Long accountId) {
        log.info("Deleting account with id: {}", accountId);
        accountService.deleteAccount(accountId);
        return ResponseEntity.noContent().build();
    }
}
