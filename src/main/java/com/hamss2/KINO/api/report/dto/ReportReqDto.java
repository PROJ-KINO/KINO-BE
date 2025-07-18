package com.hamss2.KINO.api.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class ReportReqDto {

    private int reportType;    // 신고 카테고리
    private String content; // 신고 상세 내용
    private Long relatedType;   // 상세리뷰(-1), 한줄평(-2), 댓글(게시글id)
    private Long relatedId;    // 신고 대상 ID (ex: 한줄평ID, 게시글 ID 등)

}
