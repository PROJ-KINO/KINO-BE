package com.hamss2.KINO.api.mypage.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MypageMainResDto {
    private String nickname;
    private String image;
    private ShortReviewDto shortReview;
    private ReviewDto review;
    private int followers;
    private int following;
    private List<MyPickMovieDto> myPickMoives;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ShortReviewDto {
        private Long shortReviewId;
        private String content;
        private String movieTitle;
        private LocalDateTime createdAt;
        private int rating;
        private int likes;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ReviewDto {
        private Long reviewId;
        private String title;
        private String content;
        private String movieTitle;
        private LocalDateTime createdAt;
        private int likes;
        private int views;
        private int comments;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MyPickMovieDto {
        private Long myPickId;
        private String movieTitle;
        private String posterUrl;
        private String director;
        private LocalDate releasedAt;
    }
}
