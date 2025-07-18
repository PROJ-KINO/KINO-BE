package com.hamss2.KINO.api.review.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class WritingReviewResDto {

    private Long userId;
    private String userNickname;

    private Long movieId;
    private String movieTitle;
    private String moviePosterUrl;
    private String movieReleaseDate; // YYYY-MM-DD 형식
    private String movieAverageRating;

}