package com.his.server.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtils {

    // Should be in config, but for simplicity/demo
    private static final String SECRET = "his_backend_secret_key_needs_to_be_very_long_for_hs512_algorithm_at_least_64_bytes";
    private static final long EXPIRATION_TIME = 86400000; // 24 hours

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes());
    }

    public String generateToken(Integer userId, Integer pid, String phone) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("uid", userId);
        claims.put("pid", pid);
        claims.put("phone", phone);
        return createToken(claims, String.valueOf(userId));
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    public Boolean validateToken(String token, Integer userId) {
        final Integer extractedUserId = extractUserId(token);
        return (extractedUserId.equals(userId) && !isTokenExpired(token));
    }

    public Boolean validateToken(String token) {
        return !isTokenExpired(token);
    }

    public Integer extractUserId(String token) {
        final Claims claims = extractAllClaims(token);
        return Integer.parseInt(claims.getSubject());
    }
    
    public Integer extractPid(String token) {
        final Claims claims = extractAllClaims(token);
        return claims.get("pid", Integer.class);
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
