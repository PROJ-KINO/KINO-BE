package com.hamss2.KINO.api.auth.controller;

import com.hamss2.KINO.api.auth.service.AuthService;
import com.hamss2.KINO.common.exception.BadRequestException;
import com.hamss2.KINO.common.reponse.ApiResponse;
import com.hamss2.KINO.common.reponse.SuccessStatus;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/api")
@RequiredArgsConstructor
public class TokenController {

    private final AuthService authService;

    @GetMapping("/refresh")
    public ResponseEntity<ApiResponse<String>> reissueAccessToken(
        @AuthenticationPrincipal String userId
    ) {
        if (userId == null || userId.isEmpty()) {
            throw new BadRequestException("userId is required");
        }
        Long id = Long.parseLong(userId);

        return ApiResponse.success(
            SuccessStatus.CREATE_ACCESS_TOKEN_SUCCESS,
            authService.reissueAccessToken(id)
        );
    }

    @GetMapping("/logout")
    public ResponseEntity<ApiResponse<Boolean>> logout(
        @AuthenticationPrincipal String userId,
        HttpServletRequest request
    ) {
        if (userId == null || userId.isEmpty()) {
            throw new IllegalArgumentException("사용자 ID가 제공되지 않았습니다.");
        }
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        String userId = authentication.getName();
        try {
            Long id = Long.parseLong(userId);
            ResponseEntity<ApiResponse<Boolean>> success = ApiResponse.success(
                SuccessStatus.SEND_LOGOUT_SUCCESS, authService.logout(id));

            HttpSession session = request.getSession();  // 세션 생성/조회
            session.setAttribute("isLogout", true);

            return success;

        } catch (Exception e) {
            log.error("로그아웃 중 오류 발생: {}", e.getMessage());
            throw new BadRequestException("로그아웃 처리 중 오류가 발생했습니다.");
        }

    }
}
