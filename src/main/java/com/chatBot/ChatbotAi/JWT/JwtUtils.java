package com.chatBot.ChatbotAi.JWT;

import java.security.Key;
import java.util.Date;
import java.util.Optional;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;

@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${spring.app.jwtSecret}")
    private String jwtSecret;

    @Value("${spring.app.jwtExpirationMs}")
    private Long jwtExpirationMs;

    public String generateJwtToken(String sessionKey, String app, Long jwtExpiration) {
        if (jwtExpiration == null || jwtExpiration <= 0) {
            jwtExpiration = jwtExpirationMs;
        }
        return Jwts.builder()
                .subject(sessionKey)
                .issuedAt(new Date())
                .claim("app", app)
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(key())
                .compact();
    }

    private Key key() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith((SecretKey) key())
                .build()
                .parseSignedClaims(token)
                .getPayload(); // returns all claims
    }

    public String getAuthenticationToken(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        return null;
    }

    public String getSessionFromJwtToken(String token) {
        return Jwts.parser().verifyWith((SecretKey) key()).build().parseSignedClaims(token).getPayload().getSubject();
    }

    public boolean validateJwtToken(String authToken) {
        // A valid compact JWT must have 3 parts separated by 2 periods.
        // If it doesn't, it might be an API Key or malformed token, so we fail silently.
        if (authToken == null || authToken.split("\\.").length != 3) {
            return false;
        }

        try {
            Jwts.parser().verifyWith((SecretKey) key()).build().parseSignedClaims(authToken);
            return true;
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        }
//        return subject;
        return false;
    }
}
