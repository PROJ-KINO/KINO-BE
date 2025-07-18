package com.hamss2.KINO.api.review.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class ReviewCommentResDto {

    private Long commentId;
    private String commentContent;
    private String commentCreatedAt;
    private Boolean isActive; // 활성화된 댓글인지 아닌
    private Long writerId;
    private String writerUserNickname;
    private String writerUserImage;
    private Boolean isMine; // 사용자가 작성자인지 여부

}
