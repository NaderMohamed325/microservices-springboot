package com.neo.customerservice.services.auth;

import com.neo.customerservice.dto.user.input.CreateUserInputDto;
import com.neo.customerservice.dto.user.input.LoginUserInputDto;
import com.neo.customerservice.dto.user.output.LoginUserOutputDto;
import com.neo.customerservice.dto.user.output.UserOutputDto;
import com.neo.customerservice.services.jwt.JwtService;
import com.neo.customerservice.services.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {


    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final UserService userService;

    @Override
    public LoginUserOutputDto login(LoginUserInputDto loginUserInputDto) {
        log.info("Login attempt for user: {}", loginUserInputDto.getUsername());
        UserDetails userDetails = userDetailsService.loadUserByUsername(loginUserInputDto.getUsername());

        String token = jwtService.generateToken(userDetails);
        log.info("Token generated for user: {}", loginUserInputDto.getUsername());

        return new LoginUserOutputDto(token);
    }

    @Override
    public UserOutputDto registerUser(CreateUserInputDto createUserInputDto) {
        log.info("Registering user with username: {}", createUserInputDto.getUsername());
        return userService.createUser(createUserInputDto);
    }
}
