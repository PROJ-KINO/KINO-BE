package com.hamss2.KINO.api.auth.dto;

import lombok.Data;

@Data
public class KakaoOAuthResDto {

    private String token_type;
    private String scope;
    private String access_token;
    private String refresh_token;
    private Long expires_in;
    private Long refresh_token_expires_in;

}
