package com.hamss2.KINO.common.jwt;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
// 인증에 실패했을 때 401 Unauthorized 에러를 리턴할 클래스
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
        AuthenticationException authException) throws IOException, ServletException {
        // 인증에 실패하면 401 Unauthorized 에러를 리턴
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
    }
}
