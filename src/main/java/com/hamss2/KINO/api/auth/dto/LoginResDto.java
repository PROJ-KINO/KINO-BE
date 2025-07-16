package com.hamss2.KINO.api.auth.dto;

import lombok.Data;

@Data
public class LoginResDto {

    private String accessToken; // JWT 토큰
    private String refreshToken; // 리프레시 토큰

}
