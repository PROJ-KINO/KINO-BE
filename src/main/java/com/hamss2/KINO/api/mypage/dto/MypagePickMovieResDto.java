package com.hamss2.KINO.api.mypage.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MypagePickMovieResDto {
    private List<MyPickMovieDto> myPickMovies;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MyPickMovieDto {
        private Long myPickId;
        private String movieTitle;
        private String posterUrl;
        private String director;
        private LocalDate releaseDate;
    }
}
