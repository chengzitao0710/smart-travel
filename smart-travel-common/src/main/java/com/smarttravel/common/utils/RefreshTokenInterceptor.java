package com.smarttravel.common.utils;

import com.smarttravel.common.dto.UserDTO;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

public class RefreshTokenInterceptor implements HandlerInterceptor {

    private static final long REFRESH_THRESHOLD_MS = 5 * 60 * 1000L;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String tokenHeader = request.getHeader("authorization");
        if (tokenHeader == null || !tokenHeader.startsWith("Bearer ")) {
            return true;
        }
        String token = tokenHeader.substring(7);
        if (!JwtUtils.validateToken(token)) {
            return true;
        }
        Claims claims = JwtUtils.parseToken(token);
        UserDTO userDTO = UserDTO.builder()
                .id(claims.get("id", Long.class))
                .nickname(claims.get("nickname", String.class))
                .icon(claims.get("icon", String.class))
                .build();
        UserHolder.setUser(userDTO);

        if (JwtUtils.isTokenExpiringSoon(token, REFRESH_THRESHOLD_MS)) {
            Integer tokenVersion = JwtUtils.getTokenVersionFromToken(token);
            String newToken = JwtUtils.generateToken(userDTO.getId(), userDTO.getNickname(), userDTO.getIcon(), tokenVersion);
            response.setHeader("Authorization", "Bearer " + newToken);
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserHolder.removeUser();
    }
}