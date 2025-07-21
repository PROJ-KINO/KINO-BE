package com.hamss2.KINO.api.admin.controller;

import com.hamss2.KINO.api.admin.dto.*;
import com.hamss2.KINO.api.admin.service.AdminService;
import com.hamss2.KINO.common.reponse.ApiResponse;
import com.hamss2.KINO.common.reponse.SuccessStatus;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
        Page<AdminReviewResDto> dtos = adminService.getReportShortReviews(
            PageRequest.of(page, size));
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
    public ResponseEntity<ApiResponse<AdminReportShortReviewDetailResDto>> shortreviewdetail(
        @PathVariable Long reportId) {
        AdminReportShortReviewDetailResDto dtos = adminService.getReportShortReviewDetail(reportId);
        return ApiResponse.success(SuccessStatus.SEARCH_ADMIN_SHORTREVIEW_DETAIL_SUCCESS, dtos);
    }

    @GetMapping("/reviewdetail/{reportId}")
    public ResponseEntity<ApiResponse<AdminReportReviewDetailResDto>> reviewdetail(
        @PathVariable Long reportId, @RequestParam(defaultValue = "EN") String lang) {
        AdminReportReviewDetailResDto dtos = adminService.getReportReviewDetail(reportId);
        return ApiResponse.success(SuccessStatus.SEARCH_ADMIN_REVIEW_DETAIL_SUCCESS, dtos);
    }

    @GetMapping("/commentdetail/{reportId}")
    public ResponseEntity<ApiResponse<AdminReportCommentDetailResDto>> commentdetail(
        @PathVariable Long reportId) {
        AdminReportCommentDetailResDto dtos = adminService.getReportCommentDetail(reportId);
        return ApiResponse.success(SuccessStatus.SEARCH_ADMIN_COMMENT_DETAIL_SUCCESS, dtos);
    }

    @PostMapping("/active")
    public ResponseEntity<ApiResponse<Void>> active(@RequestBody List<Long> ids) {
        adminService.active(ids);
        return ApiResponse.success_only(SuccessStatus.USERBAN_DISABLE_SUCCESS);
    }

    @PostMapping("/process")
    public ResponseEntity<ApiResponse<Void>> process(@RequestBody AdminReqDto adminReqDto) {
        adminService.deleteReport(adminReqDto.getReportId());
        if (adminReqDto.getReportedId() != 0) {
            adminService.process(adminReqDto);
        }
        return ApiResponse.success_only(SuccessStatus.PROCESS_REPORT_SUCCESS);
    }

    @GetMapping("/stats/banuser/month")
    public ResponseEntity<ApiResponse<List<BanUserMonthStatDto>>> getBanUserMonthStat(
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ApiResponse.success(SuccessStatus.SEARCH_ADMIN_BANUSER_MONTH_STAT_SUCCESS, adminService.getMonthlyBanUserStat(start, end));
    }

    @GetMapping("/stats/shortreview/genre")
    public ResponseEntity<ApiResponse<List<GenreShortReviewStatDto>>> getGenreShortReviewRank() {
        List<GenreShortReviewStatDto> result = adminService.getGenreShortReviewRank();
        return ApiResponse.success(SuccessStatus.SEARCH_ADMIN_SHORTREVIEW_GENRE_STAT_SUCCESS, result);
    }
}
