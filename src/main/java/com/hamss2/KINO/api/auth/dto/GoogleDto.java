package com.hamss2.KINO.api.auth.dto;

import lombok.Data;

@Data
public class GoogleDto {

    private String id; // Google ID
    private String email; // Google 이메일 주소
    private String name; // Google 사용자 이름
    private String picture;
    private Boolean verified_email;
    private String given_name;
    private String family_name;

    public UserInfo toUserInfo() {
        UserInfo userInfo = new UserInfo();
        userInfo.setProviderId(id);
        userInfo.setNickname(name);
        userInfo.setEmail(email);
        userInfo.setImage(null);
        return userInfo;
    }

}