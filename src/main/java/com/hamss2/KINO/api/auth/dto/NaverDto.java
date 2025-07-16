package com.hamss2.KINO.api.auth.dto;

import lombok.Data;

@Data
public class NaverDto {

    private String resultcode;
    private String message;
    private Response response;
}

@Data
class Response {

    private String nickname;
    private String profile_image;
    private String email;
}
