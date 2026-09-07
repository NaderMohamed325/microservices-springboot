package com.neo.customerservice.services.jwt;

import com.neo.customerservice.entity.User;
import com.neo.customerservice.enums.UserRoles;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class JwtServiceImplTest {

    private static final String SECRET = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private static final long EXPIRATION_TIME = 3600000; // 1 hour
    @InjectMocks
    private JwtServiceImpl jwtService;
    private User testUser;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtService, "SECRET", SECRET);
        ReflectionTestUtils.setField(jwtService, "EXPIRATION_TIME", EXPIRATION_TIME);

        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .role(UserRoles.CUSTOMER)
                .addresses(new ArrayList<>())
                .build();
    }

    @Test
    void generateToken_validUser_returnsToken() {
        String token = jwtService.generateToken(testUser);

        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    void extractUsername_validToken_returnsUsername() {
        String token = jwtService.generateToken(testUser);

        String extractedUsername = jwtService.extractUsername(token);

        assertThat(extractedUsername).isEqualTo("testuser");
    }

    @Test
    void isTokenValid_validTokenAndMatchingUser_returnsTrue() {
        String token = jwtService.generateToken(testUser);

        boolean isValid = jwtService.isTokenValid(token, testUser);

        assertThat(isValid).isTrue();
    }

    @Test
    void isTokenValid_expiredToken_returnsFalse() {
        ReflectionTestUtils.setField(jwtService, "EXPIRATION_TIME", -1);
        String token = jwtService.generateToken(testUser);

        assertThatThrownBy(() -> jwtService.isTokenValid(token, testUser))
                .isInstanceOf(io.jsonwebtoken.ExpiredJwtException.class);
    }

    @Test
    void isTokenValid_wrongUsername_returnsFalse() {
        String token = jwtService.generateToken(testUser);

        User differentUser = User.builder()
                .id(2L)
                .username("differentuser")
                .email("different@example.com")
                .password("encodedPassword")
                .role(UserRoles.CUSTOMER)
                .addresses(new ArrayList<>())
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
        assertThatThrownBy(() -> jwtService.isTokenValid("invalid.token.here", testUser))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void generateToken_thenExtractUsername_roundTrip() {
        String token = jwtService.generateToken(testUser);

        String extractedUsername = jwtService.extractUsername(token);

        assertThat(extractedUsername).isEqualTo(testUser.getUsername());
    }

    @Test
    void isTokenValid_validTokenAndMatchingUser_tokenStructure() {
        String token = jwtService.generateToken(testUser);

        String[] parts = token.split("\\.");
        assertThat(parts).hasSize(3);
        assertThat(parts[0]).isNotEmpty();
        assertThat(parts[1]).isNotEmpty();
        assertThat(parts[2]).isNotEmpty();
    }
}
