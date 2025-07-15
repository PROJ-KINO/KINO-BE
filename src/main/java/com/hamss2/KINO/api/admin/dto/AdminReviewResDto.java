package com.hamss2.KINO.api.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminReviewResDto {
    private Long reportId;
    private String reporterEmail;
    private String reportedEmail;
    private String reportedRole;
    private LocalDateTime reportedDate;
}
