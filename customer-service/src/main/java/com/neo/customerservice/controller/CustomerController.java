package com.neo.customerservice.controller;

import com.neo.customerservice.dto.user.input.UpdateUserInputDto;
import com.neo.customerservice.dto.user.output.UserOutputDto;
import com.neo.customerservice.entity.User;
import com.neo.customerservice.services.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@Tag(name = "Customers", description = "Customer management operations")
public class CustomerController {

    private final UserService userService;

    @GetMapping()
    @PreAuthorize("hasRole('ADMIN') or hasRole('CUSTOMER')")
    @Operation(summary = "Get all customers", description = "Retrieve paginated and sorted list of customers")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Customers retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    public Page<UserOutputDto> getAllCustomers(
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Field to sort by", example = "id")
            @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Sort direction (asc or desc)", example = "asc")
            @RequestParam(defaultValue = "asc") String sortDir) {
        return userService.getCustomersPaginated(page, size, sortBy, sortDir);
    }

    @GetMapping("{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CUSTOMER')")
    @Operation(summary = "Get customer by ID", description = "Retrieve a specific customer by their ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Customer found and returned"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    public ResponseEntity<UserOutputDto> getCustomerById(
            @Parameter(description = "Customer ID", required = true, example = "1")
            @PathVariable("id") Long customerId) {
        UserOutputDto userOutputDto = userService.getUserByIdOutputDto(customerId);
        return ResponseEntity.ok(userOutputDto);
    }

    @DeleteMapping("{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete customer", description = "Delete a customer by ID (ADMIN only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Customer deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - ADMIN role required"),
            @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    public ResponseEntity<Void> deleteCustomer(
            @Parameter(description = "Customer ID", required = true, example = "1")
            @PathVariable("id") Long customerId) {
        userService.deleteUser(customerId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping()
    @PreAuthorize("hasRole('ADMIN') or hasRole('CUSTOMER')")
    @Operation(summary = "Update customer profile", description = "Update the authenticated user's profile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Customer updated successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    public ResponseEntity<UserOutputDto> updateCustomer(@AuthenticationPrincipal User actor, @Valid @RequestBody UpdateUserInputDto updateUserInputDto) {
        UserOutputDto updatedUser = userService.updateUser(actor.getId(), updateUserInputDto);
        return ResponseEntity.ok(updatedUser);
    }
}
