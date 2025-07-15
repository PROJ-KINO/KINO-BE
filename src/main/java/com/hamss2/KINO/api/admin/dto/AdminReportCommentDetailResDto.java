package com.hamss2.KINO.api.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminReportCommentDetailResDto {
    private Long reportedId;
    private Long reportId;
    private Long relatedId;
    private int relatedType;
    private int reportType;
    private String reporterEmail;
    private String reportedEmail;
    private LocalDateTime reportedDate;
    private String content;
    private String reportContent;
}
