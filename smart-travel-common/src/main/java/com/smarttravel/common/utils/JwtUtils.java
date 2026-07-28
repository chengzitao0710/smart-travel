package com.smarttravel.common.utils;

import cn.hutool.core.lang.UUID;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;

@Component
public class JwtUtils {

    private static String SECRET;

    private static long EXPIRATION;

    @Value("${jwt.secret}")
    public void setSecret(String secret) {
        SECRET = secret;
    }

    @Value("${jwt.expiration}")
    public void setExpiration(long expiration) {
        EXPIRATION = expiration;
    }

    private static SecretKey getSecretKey() {
        byte[] keyBytes = SECRET.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public static String generateToken(Long userId, String nickName, String icon, Integer tokenVersion) {
        HashMap<String, Object> claims = new HashMap<>();
        claims.put("id", userId);
        claims.put("nickname", nickName);
        claims.put("icon", icon);
        claims.put("tokenVersion", tokenVersion);

        String jti = UUID.randomUUID().toString();
        return Jwts.builder()
                .setClaims(claims)
                .setId(jti)
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(getSecretKey())
                .compact();
    }

    public static String getJtiFromToken(String token) {
        return parseToken(token).getId();
    }

    public static Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSecretKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public static Long getUserIdFromToken(String token) {
        return parseToken(token).get("id", Long.class);
    }

    public static String getNickNameFromToken(String token) {
        return parseToken(token).get("nickname", String.class);
    }

    public static String getIconFromToken(String token) {
        return parseToken(token).get("icon", String.class);
    }

    public static Integer getTokenVersionFromToken(String token) {
        return parseToken(token).get("tokenVersion", Integer.class);
    }

    public static boolean isTokenExpired(String token) {
        try {
            return parseToken(token).getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    public static boolean validateToken(String token) {
        try {
            parseToken(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    public static long getTokenTtlMs(String token) {
        return parseToken(token).getExpiration().getTime() - System.currentTimeMillis();
    }

    public static boolean isTokenExpiringSoon(String token, long withinMs) {
        try {
            long ttl = getTokenTtlMs(token);
            return ttl > 0 && ttl <= withinMs;
        } catch (Exception e) {
            return false;
        }
    }
}