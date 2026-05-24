package com.att.tdp.issueflow.security;

import com.att.tdp.issueflow.entities.Role;
import com.att.tdp.issueflow.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collection;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("null")
class JwtUtilTest {

    private JwtUtil jwtUtil;
    private User user;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", "TestSecretKeyForJwtUtilTestingPurposes1234567890");
        ReflectionTestUtils.setField(jwtUtil, "expirationMs", 86400000L);

        user = User.builder()
                .id(1L)
                .username("alice")
                .password("hashed")
                .fullName("Alice")
                .email("alice@example.com")
                .role(Role.DEVELOPER)
                .build();
    }

    @Test
    void generateToken_returnsNonEmptyToken() {
        String token = jwtUtil.generateToken(user);

        assertNotNull(token);
        assertFalse(token.isBlank());
        // JWTs have three dot-separated parts (header.payload.signature)
        assertEquals(3, token.split("\\.").length);
    }

    @Test
    void extractUsername_returnsSubjectFromToken() {
        String token = jwtUtil.generateToken(user);

        String username = jwtUtil.extractUsername(token);

        assertEquals("alice", username);
    }

    @Test
    void extractRole_returnsRoleClaimFromToken() {
        String token = jwtUtil.generateToken(user);

        String role = jwtUtil.extractRole(token);

        assertEquals("DEVELOPER", role);
    }

    @Test
    void isTokenValid_returnsTrueForMatchingUser() {
        String token = jwtUtil.generateToken(user);
        UserDetails userDetails = stubUserDetails("alice");

        assertTrue(jwtUtil.isTokenValid(token, userDetails));
    }

    @Test
    void isTokenValid_returnsFalseForDifferentUser() {
        String token = jwtUtil.generateToken(user);
        UserDetails userDetails = stubUserDetails("bob");

        assertFalse(jwtUtil.isTokenValid(token, userDetails));
    }

    @Test
    void isTokenValid_returnsFalseForTamperedToken() {
        String token = jwtUtil.generateToken(user);
        String tampered = token.substring(0, token.length() - 4) + "XXXX";
        UserDetails userDetails = stubUserDetails("alice");

        assertFalse(jwtUtil.isTokenValid(tampered, userDetails));
    }

    @Test
    void isTokenValid_returnsFalseForExpiredToken() {
        ReflectionTestUtils.setField(jwtUtil, "expirationMs", -1000L);
        String expiredToken = jwtUtil.generateToken(user);
        UserDetails userDetails = stubUserDetails("alice");

        assertFalse(jwtUtil.isTokenValid(expiredToken, userDetails));
    }

    private UserDetails stubUserDetails(String username) {
        return new UserDetails() {
            @Override
            public Collection<? extends org.springframework.security.core.GrantedAuthority> getAuthorities() {
                return Collections.emptyList();
            }

            @Override
            public String getPassword() { return "x"; }

            @Override
            public String getUsername() { return username; }

            @Override
            public boolean isAccountNonExpired() { return true; }

            @Override
            public boolean isAccountNonLocked() { return true; }

            @Override
            public boolean isCredentialsNonExpired() { return true; }

            @Override
            public boolean isEnabled() { return true; }
        };
    }
}
