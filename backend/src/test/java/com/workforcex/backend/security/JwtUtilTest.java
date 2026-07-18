package com.workforcex.backend.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    private static final String SECRET = "ThisIsAValidAndSufficientlyLongSecretKeyForTestingPurposesOnly12345";

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", SECRET);
        ReflectionTestUtils.setField(jwtUtil, "expirationMs", 86400000L);
    }

    @Test
    void generateToken_thenExtractMobileNumberAndRole_roundTrips() {
        String token = jwtUtil.generateToken("+919876543210", "WORKER");

        assertThat(jwtUtil.extractMobileNumber(token)).isEqualTo("+919876543210");
        assertThat(jwtUtil.extractRole(token)).isEqualTo("WORKER");
    }

    @Test
    void isTokenValid_matchingMobileNumberAndNotExpired_returnsTrue() {
        String token = jwtUtil.generateToken("+919876543210", "EMPLOYER");

        assertThat(jwtUtil.isTokenValid(token, "+919876543210")).isTrue();
    }

    @Test
    void isTokenValid_mismatchedMobileNumber_returnsFalse() {
        String token = jwtUtil.generateToken("+919876543210", "EMPLOYER");

        assertThat(jwtUtil.isTokenValid(token, "+919999999999")).isFalse();
    }

    @Test
    void isTokenValid_expiredToken_returnsFalse() {
        ReflectionTestUtils.setField(jwtUtil, "expirationMs", -1000L);
        String token = jwtUtil.generateToken("+919876543210", "WORKER");

        assertThat(jwtUtil.isTokenValid(token, "+919876543210")).isFalse();
    }

    @Test
    void isTokenValid_malformedToken_returnsFalseInsteadOfThrowing() {
        assertThat(jwtUtil.isTokenValid("not-a-real-token", "+919876543210")).isFalse();
    }

    @Test
    void isTokenValid_tokenSignedWithDifferentKey_returnsFalse() {
        SecretKey otherKey = Keys.hmacShaKeyFor(
                "AnotherCompletelyDifferentSecretKeyThatIsAlsoLongEnough999".getBytes());
        String foreignToken = Jwts.builder()
                .subject("+919876543210")
                .claim("role", "WORKER")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 100000))
                .signWith(otherKey)
                .compact();

        assertThat(jwtUtil.isTokenValid(foreignToken, "+919876543210")).isFalse();
    }
}
