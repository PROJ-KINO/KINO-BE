package com.hamss2.KINO.api.auth.controller;

import com.hamss2.KINO.api.auth.service.AuthService;
import com.hamss2.KINO.common.exception.BadRequestException;
import com.hamss2.KINO.common.reponse.ApiResponse;
import com.hamss2.KINO.common.reponse.SuccessStatus;
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
    public ResponseEntity<ApiResponse<String>> reissueAccessToken() {
        return ApiResponse.success(
            SuccessStatus.CREATE_ACCESS_TOKEN_SUCCESS,
            authService.reissueAccessToken()
        );
    }

    @GetMapping("/logout")
    public ResponseEntity<ApiResponse<Boolean>> logout(@AuthenticationPrincipal String userId) {
        if (userId == null || userId.isEmpty()) {
            throw new IllegalArgumentException("사용자 ID가 제공되지 않았습니다.");
        }
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        String userId = authentication.getName();
        try {
            Long id = Long.parseLong(userId);
            return ApiResponse.success(SuccessStatus.SEND_LOGOUT_SUCCESS, authService.logout(id));
        } catch (Exception e) {
            log.error("로그아웃 중 오류 발생: {}", e.getMessage());
            throw new BadRequestException("로그아웃 처리 중 오류가 발생했습니다.");
        }

    }
}
