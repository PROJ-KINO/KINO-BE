package com.hamss2.KINO.common.jwt;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component // Bean으로 등록
@Slf4j
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    @Override // 권한이 없는 경우 403 Forbidden 에러를 리턴할 클래스
    public void handle(
        HttpServletRequest request,
        HttpServletResponse response,
        AccessDeniedException accessDeniedException
    ) throws IOException, ServletException {

        log.error("권한이 없는 요청: {}", request.getRequestURI());
        log.error("Access Denied Exception: {}", accessDeniedException.getMessage());
        response.sendError(HttpServletResponse.SC_FORBIDDEN); // 권한이 없으면 403 Forbidden 에러를 리턴
    }
}
