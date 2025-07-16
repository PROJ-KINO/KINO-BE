package com.hamss2.KINO.api.movieDetail.dto.req;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShortReviewReqDto {
    private int rating;
    private String content;
}
