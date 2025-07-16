package com.hamss2.KINO.api.auth.dto;

import lombok.Data;

@Data
public class UserInfo {

    private String providerId;
    private String nickname;
    private String email;
    private String image;

}
