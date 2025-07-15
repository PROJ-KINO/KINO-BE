package com.hamss2.KINO.api.mypage.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MypageReviewResDto {
    private List<ReviewDto> reviews;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ReviewDto {
        private Long reviewId;
        private String title;
        private String content;
        private String movieTitle;
        private Integer totalViews;
        private LocalDateTime createdAt;
    }
}
