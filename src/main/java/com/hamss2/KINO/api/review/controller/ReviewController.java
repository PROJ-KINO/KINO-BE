package com.hamss2.KINO.api.review.controller;

import com.hamss2.KINO.api.review.dto.ReviewDetailResDto;
import com.hamss2.KINO.api.review.dto.ReviewReqDto;
import com.hamss2.KINO.api.review.dto.WritingReviewResDto;
import com.hamss2.KINO.api.review.service.ReviewDetailService;
import com.hamss2.KINO.common.exception.BadRequestException;
import com.hamss2.KINO.common.reponse.ApiResponse;
import com.hamss2.KINO.common.reponse.SuccessStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/api/review")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewDetailService reviewService;

    // 리뷰 작성 페이지로 이동
    @GetMapping
    public ResponseEntity<ApiResponse<WritingReviewResDto>> startReview(
        @AuthenticationPrincipal String userId,
        @RequestParam(required = false) Long movieId
    ) {
        if (userId == null || userId.isEmpty()) {
            throw new BadRequestException("userId is required");
        }
        Long id = Long.parseLong(userId);
        return ApiResponse.success(SuccessStatus.REVIEW_WRITING_PAGE_SUCCESS,
            reviewService.startReview(id, movieId));

    }

    // 리뷰 작성하기
    @PostMapping
    public ResponseEntity<ApiResponse<Boolean>> createReview(
        @AuthenticationPrincipal String userId,
        @RequestBody ReviewReqDto reviewReqDto
    ) {

        if (userId == null || userId.isEmpty()) {
            throw new BadRequestException("userId is required");
        }
        Long id = Long.parseLong(userId);
        return ApiResponse.success(SuccessStatus.CREATE_REVIEW_SUCCESS,
            reviewService.createReview(id, reviewReqDto));
    }

    // 리뷰 페이지
    @GetMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<ReviewDetailResDto>> getReviewDetail(
        @AuthenticationPrincipal String userId,
        @PathVariable Long reviewId
    ) {
        if (userId == null || userId.isEmpty()) {
            throw new BadRequestException("userId is required");
        }
        Long id = Long.parseLong(userId);
        return ApiResponse.success(SuccessStatus.REVIEW_DETAIL_PAGE_SUCCESS,
            reviewService.getReviewDetail(id, reviewId));
    }
}
