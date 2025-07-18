package com.hamss2.KINO.api.admin.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminReportCommentDetailResDto {

    private Long reportedId;
    private Long reportId;
    private Long relatedId;
    private Long relatedType;
    private int reportType;
    private String reporterEmail;
    private String reportedEmail;
    private LocalDateTime reportedDate;
    private String content;
    private String reportContent;
}
