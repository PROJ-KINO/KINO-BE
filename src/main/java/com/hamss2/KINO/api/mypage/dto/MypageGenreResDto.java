package com.hamss2.KINO.api.mypage.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MypageGenreResDto {
    private String nickname;
    private int followers;
    private int following;
    private List<UserGenreDto> userGenres;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserGenreDto {
        private Long userGenreId;
        private String genreName;
    }
}
