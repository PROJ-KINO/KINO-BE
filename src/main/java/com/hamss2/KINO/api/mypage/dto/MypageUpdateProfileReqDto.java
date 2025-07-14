package com.hamss2.KINO.api.mypage.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MypageUpdateProfileReqDto {
    private String nickname;
    private MultipartFile file;
}
