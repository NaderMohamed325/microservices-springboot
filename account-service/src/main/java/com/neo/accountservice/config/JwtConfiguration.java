package com.neo.accountservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Configuration
public class JwtConfiguration {


    @Value("${spring.security.jwt.secret}")
    private String SECRET_KEY;

    @Bean
    public JwtDecoder jwtDecoder() {
        byte[] decodedKey = Base64.getDecoder().decode(SECRET_KEY);

        SecretKeySpec key = new SecretKeySpec(decodedKey, "HmacSHA256");

        return NimbusJwtDecoder.withSecretKey(key).build();
    }

}
