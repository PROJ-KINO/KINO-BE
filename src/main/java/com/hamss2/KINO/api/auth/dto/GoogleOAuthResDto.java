package com.hamss2.KINO.api.auth.dto;

import lombok.Data;

@Data
public class GoogleOAuthResDto {

    private String token_type;
    private String access_token;
    private String refresh_token;
    private Long expires_in;
    private String scope;
    private String id_token;

}
