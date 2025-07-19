package com.hamss2.KINO.common.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hamss2.KINO.common.exception.UnauthorizedException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getRequestURI();

        // 여기에 허용할 url을 추가
        if (path.startsWith("/api/auth") || path.startsWith("/api/admin") || path.startsWith(
            "/swagger-ui") || path.startsWith("/v3/api-docs")) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = resolveToken(request); // 헤더에서 JWT 추출

        if (jwt == null || jwt.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
            response.setContentType("application/json;charset=UTF-8");

            Map<String, Object> result = new HashMap<>();
            result.put("status", HttpServletResponse.SC_UNAUTHORIZED);
            result.put("success", false);
            result.put("message", "Token is empty");
            result.put("data", "Token is empty");

            ObjectMapper objectMapper = new ObjectMapper();
            String responseBody = objectMapper.writeValueAsString(result);

            response.getWriter().write(responseBody);
            response.getWriter().flush();
            response.getWriter().close();
            return;
        }

        try {
            if (StringUtils.hasText(jwt) && jwtUtils.validateToken(jwt)) {
                Authentication authentication = jwtUtils.getAuthentication(jwt);
                SecurityContextHolder.getContext().setAuthentication(authentication); // 인증 객체 설정
            }
        } catch (UnauthorizedException e) {
            log.error("JWT 인증 실패: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
            response.setContentType("application/json;charset=UTF-8");

            Map<String, Object> result = new HashMap<>();
            result.put("status", HttpServletResponse.SC_UNAUTHORIZED);
            result.put("success", false);
            result.put("message", "Unauthorized: " + e.getMessage());
            result.put("data", null);

            ObjectMapper objectMapper = new ObjectMapper();
            String responseBody = objectMapper.writeValueAsString(result);

            response.getWriter().write(responseBody);
            response.getWriter().flush();
            response.getWriter().close();
            return;
        }

        filterChain.doFilter(request, response); // 다음 필터로 요청 전달
    }

    // Authorization 헤더에서 토큰 추출
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // "Bearer " 부분 제거
        }
        return null;
    }
}
