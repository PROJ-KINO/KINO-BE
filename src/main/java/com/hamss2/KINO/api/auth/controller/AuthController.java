package com.hamss2.KINO.api.auth.controller;

import com.hamss2.KINO.api.auth.dto.SocialType;
import com.hamss2.KINO.api.auth.service.AuthService;
import com.hamss2.KINO.common.exception.BadRequestException;
import com.hamss2.KINO.common.exception.InternalServerException;
import com.hamss2.KINO.common.reponse.ApiResponse;
import com.hamss2.KINO.common.reponse.SuccessStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @GetMapping("/login/{provider}")
    public ResponseEntity<ApiResponse<String>> getLoginPage(@PathVariable String provider) {
        try {
            SocialType socialType = SocialType.valueOf(provider);
            return ApiResponse.success(
                SuccessStatus.REDIRECT_OAUTH_PAGE_SUCCESS,
                authService.getLoginPage(socialType)
            );
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("지원하지 않는 로그인 제공자입니다." + e.getMessage());
        }
    }

    @GetMapping("/oauth/{provider}")
    public void socialLogin(
        @PathVariable String provider,
        @RequestParam String code, // 인증 코드
        @RequestParam(required = false) String state
    ) {
        log.info("google login code={}, state={}", code, state);
        if (code == null || code.isEmpty()) {
            throw new InternalServerException(provider + "에서 인증 코드가 전송되지 않았습니다.");
        }

        try {
            SocialType socialType = SocialType.fromString(provider);
            authService.socialLogin(socialType, code, state);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("지원하지 않는 로그인 제공자입니다." + e.getMessage());
        }
    }

    @PostMapping("/logout")
    public void logout() {
        // 로그아웃
        // 발급된 accessToken, refreshToken으로 접근 못하게
    }

    @PostMapping("/refresh")
    public void refresh() {
        // 토큰 갱신
        // accessToken, refreshToken 재발급
    }

}
