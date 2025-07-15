package com.hamss2.KINO.api.movieDetail.dto.res;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ShortReviewResDto {
    private Long shortReviewId;
    private Long userId;
    private String userNickname;
    private String userProfile;
    private int rating;
    private String content;
    private LocalDateTime createdAt;
    private boolean mine;
    private int likeCount;
    private boolean liked; // 내가 좋아요 눌렀는지
}
