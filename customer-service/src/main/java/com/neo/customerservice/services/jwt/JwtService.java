package com.neo.customerservice.services.jwt;

import com.neo.customerservice.entity.User;
import org.springframework.security.core.userdetails.UserDetails;

public interface JwtService {

    public String generateToken(User user);


    public String extractUsername(String token);

    public boolean isTokenValid(String token, UserDetails userDetails);

}
