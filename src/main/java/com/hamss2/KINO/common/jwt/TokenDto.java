package com.hamss2.KINO.common.jwt;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TokenDto { // 토근 발급 정보

    private String grantType; // 토큰 타입 (예: Bearer)
    private String accessToken; // Access Token
    private Long accessTokenExpiresIn; // 토큰 만료 시간 (밀리초 단위)
    private String refreshToken;
    private Long refreshTokenExpiresIn;


}
