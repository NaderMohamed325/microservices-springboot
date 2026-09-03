package com.neo.customerservice.controller;

import com.neo.customerservice.dto.user.input.CreateUserInputDto;
import com.neo.customerservice.dto.user.input.LoginUserInputDto;
import com.neo.customerservice.dto.user.output.LoginUserOutputDto;
import com.neo.customerservice.dto.user.output.UserOutputDto;
import com.neo.customerservice.services.auth.AuthService;
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
public class AuthController {

    private final AuthService authService;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/register")
    public ResponseEntity<UserOutputDto> register(@RequestBody CreateUserInputDto dto) {
        UserOutputDto user = this.authService.registerUser(dto);
        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginUserOutputDto> authenticateAndGetToken(@RequestBody LoginUserInputDto authRequest) {
        try {
            LoginUserOutputDto token = authService.login(authRequest);
            return ResponseEntity.ok(token);
        } catch (Exception e) {
            log.error("Authentication failed for user: {}", authRequest.getUsername(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

}
