package com.hamss2.KINO.api.review.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class ReviewDetailResDto {

    private Long reviewId;
    private String reviewTitle;
    private String reviewContent;
    private Integer reviewViewCount;
    private LocalDateTime reviewCreatedAt;
    private Integer reviewLikeCount;
    private Integer reviewCommentCount;

    private Long movieId;
    private String movieTitle;

    private Long writerId;
    private String writerUserNickname;
    private String writerUserImage;

    private Boolean isActive; // 사용자가 활성화된 상태인지
    private Boolean isMine; // 사용자가 작성자인지 여부
    private Boolean isHeart; // 사용자가 좋아요를 눌렀는지 여부

}
