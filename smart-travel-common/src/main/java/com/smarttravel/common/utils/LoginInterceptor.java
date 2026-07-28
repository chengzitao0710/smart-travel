package com.smarttravel.common.utils;

import com.smarttravel.common.dto.UserDTO;
import io.jsonwebtoken.Claims;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Resource
    private JwtBlackListUtils jwtBlackListUtils;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String tokenHeader = request.getHeader("authorization");
        if (tokenHeader == null || !tokenHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
        String token = tokenHeader.substring(7);
        if (jwtBlackListUtils.isInBlackList(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
        if (!jwtBlackListUtils.isTokenVersionMatch(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
        if (!JwtUtils.validateToken(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
        Long userId = JwtUtils.getUserIdFromToken(token);
        String activeToken = stringRedisTemplate.opsForValue().get(RedisConstants.LOGIN_USER_KEY + userId);
        if (activeToken == null || !activeToken.equals(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
        Claims claims = JwtUtils.parseToken(token);
        UserDTO userDTO = UserDTO.builder()
                .id(claims.get("id", Long.class))
                .nickname(claims.get("nickname", String.class))
                .icon(claims.get("icon", String.class))
                .build();
        UserHolder.setUser(userDTO);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserHolder.removeUser();
    }
}