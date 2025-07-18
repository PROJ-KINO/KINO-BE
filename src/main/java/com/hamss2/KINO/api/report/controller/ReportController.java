package com.hamss2.KINO.api.report.controller;

import com.hamss2.KINO.api.report.dto.ReportReqDto;
import com.hamss2.KINO.api.report.service.ReportService;
import com.hamss2.KINO.common.exception.InternalServerException;
import com.hamss2.KINO.common.reponse.ApiResponse;
import com.hamss2.KINO.common.reponse.SuccessStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/api/report")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;


    @PostMapping("/comment")
    public ResponseEntity<ApiResponse<Boolean>> reportReview(
        @AuthenticationPrincipal String userId,
        @RequestBody ReportReqDto reportReqDto
    ) {
        if (userId == null || userId.isEmpty()) {
            throw new InternalServerException("userId is required");
        }
        Long id = Long.parseLong(userId);
        return ApiResponse.success(SuccessStatus.CREATE_REPORT_SUCCESS,
            reportService.reportReview(id, reportReqDto));
    }
}
