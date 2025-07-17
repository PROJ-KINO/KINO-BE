package com.hamss2.KINO.api.auth.dto;

import lombok.Data;

@Data
public class KakaoDto {

    private Long id; // 카카오 계정 ID
    private String connected_at; // 연결된 시간 (ISO 8601 형식)
    private Account kakao_account; // 카카오 계정 정보
    private Properties properties;

    public UserInfo toUserInfo() {
        UserInfo userInfo = new UserInfo();
        userInfo.setProviderId(String.valueOf(id));
        userInfo.setNickname(kakao_account.getProfile().getNickname());
        userInfo.setEmail(kakao_account.getEmail());
        if (kakao_account.getProfile().is_default_image()) {
            userInfo.setImage(null);
        } else {
            userInfo.setImage(kakao_account.getProfile().getProfile_image_url());
        }
        return userInfo;
    }
}

@Data
class Account {

    private Profile profile; // 프로필 정보
    private String email; // 이메일 주소 (has_email이 true일 때만 제공됨)
    private Boolean has_email;
    private Boolean email_needs_agreement;
    private Boolean is_email_valid;
    private Boolean is_email_verified;

}

@Data
class Profile {

    private String nickname; // 사용자 닉네임
    private boolean is_default_image; // 기본 프로필 이미지 여부(true: 기본 이미지, false: 사용자 지정 이미지)
    private String profile_image_url; // 프로필 이미지 URL
    private boolean is_default_nickname;
    private String thumbnail_image_url;

}

@Data
class Properties {

    private String nickname;
    private String profile_image;
    private String thumbnail_image;
}