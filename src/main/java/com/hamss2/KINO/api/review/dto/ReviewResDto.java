package com.hamss2.KINO.api.review.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class ReviewResDto {

    private Long id;
    private String title;
    private String content;
    private Integer viewCount;
    private LocalDateTime createdAt;
    private Integer likeCount;
    private Integer commentCount;

    private Boolean isMine;
    private Boolean isHeart;

}
