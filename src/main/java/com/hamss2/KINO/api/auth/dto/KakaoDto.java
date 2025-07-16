package com.hamss2.KINO.api.auth.dto;

import lombok.Data;

@Data
public class KakaoDto {

    private Long id;
    private String connected_at;
    private Account kakao_account;

}

@Data
class Account {

    private Profile profile;
    private String email;
}

@Data
class Profile {

    private String nickname;
    private String thumbnail_image_url;
    private String profile_image_url;
    private boolean is_default_image;
    private boolean is_default_nickname;
}