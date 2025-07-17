package com.hamss2.KINO.api.movieDetail.dto.res;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ReviewResDto {
    private Long reviewId;
    private String userNickname;
    private String userProfile;
    private String title;
    private String content;
    private Integer totalViews;
    private int commentCount;
    private int likeCount;
    private boolean mine;
    private boolean liked;
    private LocalDateTime createdAt;
}
