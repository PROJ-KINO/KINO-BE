package com.hamss2.KINO.api.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class SimpleUserReqDto {

    private Long userId; // 사용자 ID
    private String nickname; // 사용자 닉네임
    private String email; // 사용자 이메일
    private String image; // 사용자 프로필 이미지 URL
    private Boolean isFirstLogin; // 첫 로그인 여부
}
