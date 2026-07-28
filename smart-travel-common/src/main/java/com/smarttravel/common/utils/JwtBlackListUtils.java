package com.smarttravel.common.utils;

import io.jsonwebtoken.Claims;
import jakarta.annotation.Resource;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Component
public class JwtBlackListUtils {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private JdbcTemplate jdbcTemplate;

    private static final String BLACK_LIST_PREFIX = "jwt:blacklist:";
    private static final String TOKEN_VERSION_PREFIX = "jwt:version:";

    public void addBlackList(String token) {
        Claims claims;
        try {
            claims = JwtUtils.parseToken(token);
        } catch (Exception e) {
            return;
        }
        String jti = claims.getId();
        long ttl = claims.getExpiration().getTime() - System.currentTimeMillis();
        if (ttl <= 0) {
            return;
        }
        stringRedisTemplate.opsForValue().set(BLACK_LIST_PREFIX + jti, "revoked", ttl, TimeUnit.MILLISECONDS);

    }

    public boolean isInBlackList(String token) {
        Claims claims;
        try {
            claims = JwtUtils.parseToken(token);
        } catch (Exception e) {
            return true;
        }
        String jti = claims.getId();
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(BLACK_LIST_PREFIX + jti));
    }

    public void updateTokenVersion(Long userId) {
        jdbcTemplate.update("UPDATE tb_traveler SET token_version = token_version + 1 WHERE id = ?", userId);
        stringRedisTemplate.opsForValue().increment(TOKEN_VERSION_PREFIX + userId);
    }

    public boolean isTokenVersionMatch(String token) {
        try {
            Long userId = JwtUtils.getUserIdFromToken(token);
            Integer tokenVersion = JwtUtils.getTokenVersionFromToken(token);
            String versionStr = stringRedisTemplate.opsForValue().get(TOKEN_VERSION_PREFIX + userId);
            int currentVersion;
            if (versionStr != null) {
                currentVersion = Integer.parseInt(versionStr);
            } else {
                currentVersion = loadTokenVersionFromDb(userId);
                stringRedisTemplate.opsForValue().set(TOKEN_VERSION_PREFIX + userId, String.valueOf(currentVersion));
            }
            return tokenVersion != null && tokenVersion == currentVersion;
        } catch (Exception e) {
            return false;
        }
    }

    private int loadTokenVersionFromDb(Long userId) {
        try {
            Integer version = jdbcTemplate.queryForObject(
                    "SELECT token_version FROM tb_traveler WHERE id = ?",
                    Integer.class,
                    userId
            );
            return version != null ? version : 0;
        } catch (EmptyResultDataAccessException e) {
            return 0;
        }
    }
}