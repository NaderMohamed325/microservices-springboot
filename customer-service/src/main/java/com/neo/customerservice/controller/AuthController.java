package com.neo.customerservice.controller;

import com.neo.customerservice.dto.user.input.CreateUserInputDto;
import com.neo.customerservice.dto.user.input.LoginUserInputDto;
import com.neo.customerservice.dto.user.output.LoginUserOutputDto;
import com.neo.customerservice.dto.user.output.UserOutputDto;
import com.neo.customerservice.services.auth.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "User registration and login operations")
public class AuthController {

    private final AuthService authService;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/register")
    @Operation(summary = "Register new user", description = "Create a new customer account with username, email, and password")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "409", description = "User already exists")
    })
    public ResponseEntity<UserOutputDto> register(@Valid @RequestBody CreateUserInputDto dto) {
        UserOutputDto user = this.authService.registerUser(dto);
        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    @Operation(summary = "Login user", description = "Authenticate user and receive JWT token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful, JWT token returned"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    public ResponseEntity<LoginUserOutputDto> authenticateAndGetToken(@Valid @RequestBody LoginUserInputDto authRequest) {
        try {
            LoginUserOutputDto token = authService.login(authRequest);
            return ResponseEntity.ok(token);
        } catch (Exception e) {
            log.error("Authentication failed for user: {}", authRequest.getUsername(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

}
