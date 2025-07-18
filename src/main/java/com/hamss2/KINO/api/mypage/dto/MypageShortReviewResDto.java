package com.hamss2.KINO.api.mypage.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MypageShortReviewResDto {
    private List<ShortReviewDto> shortReviews;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ShortReviewDto {
        private Long shortReviewId;
        private String content;
        private int rating;
        private String movieTitle;
        private LocalDateTime createdAt;
        private int likes;
        private String userNickname;
        private String userProfileUrl;
    }
}
