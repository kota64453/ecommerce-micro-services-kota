package com.ecommerce.auth.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.ecommerce.auth.client.UserServiceUserDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class JwtUtil {

    private final String secret;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;
    private final String issuer;
    private final Algorithm algorithm;

    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiration}") long accessTokenExpiration,
            @Value("${jwt.refresh-token-expiration}") long refreshTokenExpiration,
            @Value("${jwt.issuer}") String issuer) {
        this.secret = secret;
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
        this.issuer = issuer;
        this.algorithm = Algorithm.HMAC256(secret);
        log.info("{} :: JwtUtil initialized with issuer: {}", getClass().getSimpleName(), issuer);
    }

    public String generateAccessToken(UserServiceUserDto user) {
        log.info("{} :: Generating access token for userId: {}", getClass().getSimpleName(), user.getId());
        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessTokenExpiration);

        JWTCreator.Builder builder = JWT.create()
                .withIssuer(issuer)
                .withSubject(user.getId().toString())
                .withClaim("userId", user.getId().toString())
                .withClaim("email", user.getEmail())
                .withClaim("name", user.getName())
                .withClaim("phone", user.getPhone())
                .withArrayClaim("roles", new String[]{user.getRole()})
                .withIssuedAt(now)
                .withExpiresAt(expiry);

        String token = builder.sign(algorithm);
        log.info("{} :: Access token generated for userId: {}", getClass().getSimpleName(), user.getId());
        return token;
    }

    public String generateRefreshToken(UserServiceUserDto user) {
        log.info("{} :: Generating refresh token for userId: {}", getClass().getSimpleName(), user.getId());
        Date now = new Date();
        Date expiry = new Date(now.getTime() + refreshTokenExpiration);

        String token = JWT.create()
                .withIssuer(issuer)
                .withSubject(user.getId().toString())
                .withClaim("userId", user.getId().toString())
                .withClaim("email", user.getEmail())
                .withClaim("type", "refresh")
                .withIssuedAt(now)
                .withExpiresAt(expiry)
                .sign(algorithm);
        
        log.info("{} :: Refresh token generated for userId: {}", getClass().getSimpleName(), user.getId());
        return token;
    }

    public DecodedJWT verifyToken(String token) throws JWTVerificationException {
        JWTVerifier verifier = JWT.require(algorithm)
                .withIssuer(issuer)
                .build();
        return verifier.verify(token);
    }

    public boolean validateToken(String token) {
        try {
            verifyToken(token);
            return true;
        } catch (JWTVerificationException e) {
            log.warn("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    public String getUserIdFromToken(String token) {
        DecodedJWT decodedJWT = verifyToken(token);
        return decodedJWT.getClaim("userId").asString();
    }

    public String getEmailFromToken(String token) {
        DecodedJWT decodedJWT = verifyToken(token);
        return decodedJWT.getClaim("email").asString();
    }

    public List<String> getRolesFromToken(String token) {
        DecodedJWT decodedJWT = verifyToken(token);
        return decodedJWT.getClaim("roles").asList(String.class);
    }

    public long getAccessTokenExpiration() {
        return accessTokenExpiration;
    }
}
