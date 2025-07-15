package com.hamss2.KINO.api.home.dto.res;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewDto {
    private Long reviewId;
    private String reviewTitle;
    private String content;
    private Long movieId;
    private String movieTitle;
}
