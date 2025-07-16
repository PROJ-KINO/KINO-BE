package com.hamss2.KINO.api.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResDto {

    private String accessToken; // JWT 토큰
    private String refreshToken; // 리프레시 토큰

}
