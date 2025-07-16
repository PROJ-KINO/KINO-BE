package com.hamss2.KINO.api.admin.controller;

import com.hamss2.KINO.api.admin.dto.*;
import com.hamss2.KINO.api.admin.service.AdminService;
import com.hamss2.KINO.common.reponse.ApiResponse;
import com.hamss2.KINO.common.reponse.SuccessStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {
    private final AdminService adminService;

    @GetMapping("/user")
    public ResponseEntity<ApiResponse<Page<AdminUserResDto>>> user(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        Page<AdminUserResDto> dtos = adminService.getUsers(PageRequest.of(page, size));
        return ApiResponse.success(SuccessStatus.SEARCH_ADMIN_ALLUSER_SUCCESS, dtos);
    }

    @GetMapping("/review")
    public ResponseEntity<ApiResponse<Page<AdminReviewResDto>>> review(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        Page<AdminReviewResDto> dtos = adminService.getReportReviews(PageRequest.of(page, size));
        return ApiResponse.success(SuccessStatus.SEARCH_ADMIN_REVIEW_SUCCESS, dtos);
    }

    @GetMapping("/shortreview")
    public ResponseEntity<ApiResponse<Page<AdminReviewResDto>>> shortreview(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        Page<AdminReviewResDto> dtos = adminService.getReportShortReviews(PageRequest.of(page, size));
        return ApiResponse.success(SuccessStatus.SEARCH_ADMIN_SHORTREVIEW_SUCCESS, dtos);
    }

    @GetMapping("/comment")
    public ResponseEntity<ApiResponse<Page<AdminReviewResDto>>> commnet(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        Page<AdminReviewResDto> dtos = adminService.getReportComments(PageRequest.of(page, size));
        return ApiResponse.success(SuccessStatus.SEARCH_ADMIN_COMMENT_SUCCESS, dtos);
    }

    @GetMapping("/shortreviewdetail/{reportId}")
    public ResponseEntity<ApiResponse<AdminReportShortReviewDetailResDto>> shortreviewdetail(@PathVariable Long reportId) {
        AdminReportShortReviewDetailResDto dtos = adminService.getReportShortReviewDetail(reportId);
        return ApiResponse.success(SuccessStatus.SEARCH_ADMIN_SHORTREVIEW_DETAIL_SUCCESS, dtos);
    }

    @GetMapping("/reviewdetail/{reportId}")
    public ResponseEntity<ApiResponse<AdminReportReviewDetailResDto>> reviewdetail(@PathVariable Long reportId) {
        AdminReportReviewDetailResDto dtos = adminService.getReportReviewDetail(reportId);
        return ApiResponse.success(SuccessStatus.SEARCH_ADMIN_REVIEW_DETAIL_SUCCESS, dtos);
    }

    @GetMapping("/commentdetail/{reportId}")
    public ResponseEntity<ApiResponse<AdminReportCommentDetailResDto>> commentdetail(@PathVariable Long reportId) {
        AdminReportCommentDetailResDto dtos = adminService.getReportCommentDetail(reportId);
        return ApiResponse.success(SuccessStatus.SEARCH_ADMIN_COMMENT_DETAIL_SUCCESS, dtos);
    }

    @PostMapping("/active/{id}")
    public ResponseEntity<ApiResponse<Void>> active(@PathVariable Long id) {
        adminService.active(id);
        return ApiResponse.success_only(SuccessStatus.USERBAN_DISABLE_SUCCESS);
    }

    @PostMapping("/process")
    public ResponseEntity<ApiResponse<Void>> process(@RequestBody AdminReqDto adminReqDto) {
        adminService.deleteReport(adminReqDto.getReportId());
        if(adminReqDto.getReportedId() != 0) adminService.process(adminReqDto);
        return ApiResponse.success_only(SuccessStatus.PROCESS_REPORT_SUCCESS);
    }
}
