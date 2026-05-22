package com.att.tdp.issueflow.security;

import com.att.tdp.issueflow.entities.User;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret:MySuperSecretKeyForIssueFlowDevelopment1234567890}")
    private String secret;

    @Value("${jwt.expiration:86400000}")
    private long expirationMs;

    public String generateToken(User user) {
        Algorithm algorithm = Algorithm.HMAC256(secret);
        return JWT.create()
                .withSubject(user.getUsername())
                .withClaim("role", user.getRole().name())
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + expirationMs))
                .sign(algorithm);
    }

    // Decodes without verifying signature — safe for extracting claims before full validation
    public String extractUsername(String token) {
        return JWT.decode(token).getSubject();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            JWT.require(algorithm)
                    .withSubject(userDetails.getUsername())
                    .build()
                    .verify(token);
            return true;
        } catch (JWTVerificationException e) {
            return false;
        }
    }

    public String extractRole(String token) {
        DecodedJWT jwt = JWT.decode(token);
        return jwt.getClaim("role").asString();
    }
}
