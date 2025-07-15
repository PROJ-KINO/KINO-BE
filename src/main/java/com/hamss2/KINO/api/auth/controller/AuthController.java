package com.hamss2.KINO.api.auth.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    @GetMapping("/kakao")
    public void kakaoLogin() {
        // 카카오 로그인
        // OAuth2.0 인증을 위한 엔드포인트
        // accessToken, refreshToken 발급

    }

    @PostMapping("/login")
    public void login() {
        // 로그인
        // OAuth2.0 인증을 위한 엔드포인트
        // accessToken, refreshToken 발급
        // refreshToken은 DB에 저장
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
