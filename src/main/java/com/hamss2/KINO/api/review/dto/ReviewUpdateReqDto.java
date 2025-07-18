package com.hamss2.KINO.api.review.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class ReviewUpdateReqDto {

    private Long reviewId; // 리뷰 ID
    private Long movieId;
    private String reviewTitle;
    private String reviewContent;


}
