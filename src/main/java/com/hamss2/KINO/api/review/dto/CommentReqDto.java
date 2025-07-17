package com.hamss2.KINO.api.review.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class CommentReqDto {

    Long reviewId;
    String commentContent;
}
