package com.hamss2.KINO.api.review.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class ReviewResDto {

    private Long reviewId;
    private String reviewTitle;
    private String reviewContent;
    private Integer reviewViewCount;
    private String reviewCreatedAt;
    private Integer reviewLikeCount;
    private Integer reviewCommentCount;

}
