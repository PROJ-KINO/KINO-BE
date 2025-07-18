package com.hamss2.KINO.api.movieDetail.dto.req;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReportReqDto {

    //    private Long reporterId;   // 신고자
    private Long reporteeId;   // 피신고자
    private int reportType;    // 신고 카테고리
    private String content;    // 신고 상세 내용
    private Long relatedId;    // ex) 한줄평ID, 게시글 id
    private Long relatedType;   // ex)1=shortReview(구분)
}
