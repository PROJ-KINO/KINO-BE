package com.hamss2.KINO.api.auth.controller;

import static com.hamss2.KINO.api.auth.dto.LoginType.GOOGLE;
import static com.hamss2.KINO.api.auth.dto.LoginType.KAKAO;
import static com.hamss2.KINO.api.auth.dto.LoginType.NAVER;

import com.hamss2.KINO.api.auth.dto.GoogleOAuthResDto;
import com.hamss2.KINO.api.auth.dto.KakaoOAuthResDto;
import com.hamss2.KINO.api.auth.dto.NaverOAuthResDto;
import com.hamss2.KINO.api.auth.service.GoogleOAuthService;
import com.hamss2.KINO.api.auth.service.KakaoOAuthService;
import com.hamss2.KINO.api.auth.service.NaverOAuthService;
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

    private final KakaoOAuthService kakaoOAuthService;
    private final NaverOAuthService naverOAuthService;
    private final GoogleOAuthService googleOAuthService;

    // OAuth2.0 로그인 페이지로 리다이렉트
    @GetMapping("/login/{provider}")
    public ResponseEntity<ApiResponse<String>> getLoginPage(
        @PathVariable String provider
    ) {
        if (KAKAO.getType().equalsIgnoreCase(provider)) {
            String kakaoAuthUrl = kakaoOAuthService.getKakaoAuthUrl();
            return ApiResponse.success(SuccessStatus.REDIRECT_OAUTH_PAGE_SUCCESS, kakaoAuthUrl);
        } else if (NAVER.getType().equalsIgnoreCase(provider)) {
            String naverAuthUrl = naverOAuthService.getNaverAuthUrl();
            return ApiResponse.success(SuccessStatus.REDIRECT_OAUTH_PAGE_SUCCESS, naverAuthUrl);
        } else if (GOOGLE.getType().equalsIgnoreCase(provider)) {
            String googleAuthUrl = googleOAuthService.getGoogleAuthUrl();
            return ApiResponse.success(SuccessStatus.REDIRECT_OAUTH_PAGE_SUCCESS,
                googleAuthUrl);
        }
        throw new BadRequestException("지원하지 않는 로그인 제공자입니다.\n현재 제공자 : " + provider);
    }

    // OAuth2.0 인증을 위한 엔드포인트
    // 사용자정보 조회 후 저장
    // accessToken, refreshToken 발급
    // refreshToken은 DB에 저장
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

        if (KAKAO.getType().equalsIgnoreCase(provider)) {
            KakaoOAuthResDto result = kakaoOAuthService.getKakaoAccessToken(code);
            log.info("kakao login result={}", result);
            return;
        } else if (NAVER.getType().equalsIgnoreCase(provider)) {
            NaverOAuthResDto result = naverOAuthService.issueNaverAccessToken(code, state);
            log.info("naver login result={}", result);
            return;
        } else if (GOOGLE.getType().equalsIgnoreCase(provider)) {
            GoogleOAuthResDto result = googleOAuthService.issueGoogleAccessToken(code, state);
            log.info("google login result={}", result);
            return;
        }
        throw new BadRequestException("지원하지 않는 로그인 제공자입니다.\n현재 제공자 : " + provider);
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
