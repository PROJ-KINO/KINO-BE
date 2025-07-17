package com.hamss2.KINO.api.auth.dto;

import lombok.Data;

@Data
public class NaverDto {

    private String resultcode; // 성공 시 "00", 실패 시 "10"
    private String message; // 성공 시 "success", 실패 시 "fail" 등 에러 메시지
    private Response response; // 사용자 정보 응답 객체

    public UserInfo toUserInfo() {
        UserInfo userInfo = new UserInfo();
        userInfo.setProviderId(response.getId());
        userInfo.setNickname(response.getNickname());
        userInfo.setEmail(response.getEmail());
        userInfo.setImage(null);
        return userInfo;
    }
}

@Data
class Response {

    private String id; // Naver ID
    private String nickname; // 사용자 닉네임
    private String email; // 사용자 이메일
    private String profile_image; // 사용자 프로필 이미지 URL
    private String age;
    private String gender;
    private String mobile;
    private String mobile_e164;
    private String name;
    private String birthday;
    private String birthyear;

}