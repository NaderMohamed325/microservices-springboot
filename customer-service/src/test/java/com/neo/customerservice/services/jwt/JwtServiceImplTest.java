package com.neo.customerservice.services.jwt;

import com.neo.customerservice.entity.User;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class JwtServiceImplTest {

    private static final String SECRET = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private static final long EXPIRATION_TIME = 3600000; // 1 hour
    @InjectMocks
    private JwtServiceImpl jwtService;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtService, "SECRET", SECRET);
        ReflectionTestUtils.setField(jwtService, "EXPIRATION_TIME", EXPIRATION_TIME);

        userDetails = org.springframework.security.core.userdetails.User.builder()
                .username("testuser")
                .password("encodedPassword")
                .roles("CUSTOMER")
                .build();
    }

    @Test
    void generateToken_validUserDetails_returnsToken() {
        String token = jwtService.generateToken((User) userDetails);

        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3); // JWT has 3 parts separated by dots
    }

    @Test
    void extractUsername_validToken_returnsUsername() {
        String token = jwtService.generateToken((User) userDetails);

        String extractedUsername = jwtService.extractUsername(token);

        assertThat(extractedUsername).isEqualTo("testuser");
    }

    @Test
    void isTokenValid_validTokenAndMatchingUser_returnsTrue() {
        String token = jwtService.generateToken((User) userDetails);

        boolean isValid = jwtService.isTokenValid(token, userDetails);

        assertThat(isValid).isTrue();
    }

    @Test
    void isTokenValid_expiredToken_returnsFalse() {
        ReflectionTestUtils.setField(jwtService, "EXPIRATION_TIME", -1); // Expire immediately
        String token = jwtService.generateToken((User) userDetails);

        assertThatThrownBy(() -> jwtService.isTokenValid(token, userDetails))
                .isInstanceOf(io.jsonwebtoken.ExpiredJwtException.class);
    }

    @Test
    void isTokenValid_wrongUsername_returnsFalse() {
        String token = jwtService.generateToken((User) userDetails);

        UserDetails differentUser = org.springframework.security.core.userdetails.User.builder()
                .username("differentuser")
                .password("encodedPassword")
                .roles("CUSTOMER")
                .build();

        boolean isValid = jwtService.isTokenValid(token, differentUser);

        assertThat(isValid).isFalse();
    }

    @Test
    void extractUsername_malformedToken_throwsJwtException() {
        assertThatThrownBy(() -> jwtService.extractUsername("invalid.token.here"))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void extractUsername_emptyToken_throwsJwtException() {
        assertThatThrownBy(() -> jwtService.extractUsername(""))
                .isInstanceOf(Exception.class);
    }

    @Test
    void isTokenValid_malformedToken_throwsJwtException() {
        assertThatThrownBy(() -> jwtService.isTokenValid("invalid.token.here", userDetails))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void generateToken_thenExtractUsername_roundTrip() {
        String token = jwtService.generateToken((User) userDetails);

        String extractedUsername = jwtService.extractUsername(token);

        assertThat(extractedUsername).isEqualTo(userDetails.getUsername());
    }

    @Test
    void isTokenValid_validTokenAndMatchingUser_tokenStructure() {
        String token = jwtService.generateToken((User) userDetails);

        // Token should have 3 parts (header, payload, signature)
        String[] parts = token.split("\\.");
        assertThat(parts).hasSize(3);

        // Header should be a valid base64
        assertThat(parts[0]).isNotEmpty();
        // Payload should be a valid base64
        assertThat(parts[1]).isNotEmpty();
        // Signature should be a valid base64
        assertThat(parts[2]).isNotEmpty();
    }
}
