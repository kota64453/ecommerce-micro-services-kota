package com.ecommerce.apigatewayservice.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
@Slf4j
public class JwtUtil {

    private static final String ROLE_TAG = "role";

    @Value("${jwt.secret")
    private String secret;

    private DecodedJWT getDecodedToken(
            String token)
            throws JWTVerificationException {

        Algorithm algorithm =
                Algorithm.HMAC256(
                        secret.getBytes(
                                StandardCharsets.UTF_8
                        )
                );

        return JWT.require(algorithm)
                .build()
                .verify(token);
    }

    public boolean validateToken(
            String token) {

        try {

            getDecodedToken(token);

            return true;

        } catch (JWTVerificationException ex) {

            return false;
        }
    }

    public String retrieveEmailFromToken(
            String token) {

        return getDecodedToken(token)
                .getSubject();
    }

    public List<String> retrieveRolesFromToken(
            String token) {

        return getDecodedToken(token)
                .getClaim(ROLE_TAG)
                .asList(String.class);
    }
}
