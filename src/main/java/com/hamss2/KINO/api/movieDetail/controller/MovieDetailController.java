package com.hamss2.KINO.api.movieDetail.controller;

import com.hamss2.KINO.api.deepl.annotation.Translate;
import com.hamss2.KINO.api.movieDetail.dto.req.ReportReqDto;
import com.hamss2.KINO.api.movieDetail.dto.req.ShortReviewReqDto;
import com.hamss2.KINO.api.movieDetail.dto.res.MovieDetailDto;
import com.hamss2.KINO.api.movieDetail.dto.res.ReviewResDto;
import com.hamss2.KINO.api.movieDetail.dto.res.ShortReviewResDto;
import com.hamss2.KINO.api.movieDetail.service.MovieDetailService;
import com.hamss2.KINO.api.movieDetail.service.ReviewService;
import com.hamss2.KINO.api.movieDetail.service.ShortReviewService;
import com.hamss2.KINO.common.reponse.ApiResponse;
import com.hamss2.KINO.common.reponse.SuccessStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class MovieDetailController {

    private final MovieDetailService movieDetailService;
    private final ShortReviewService shortReviewService;
    private final ReviewService reviewService;

    // 찜 여부 확인
    @GetMapping("/mypick/{movieId}")
    public ResponseEntity<ApiResponse<Boolean>> isMyPick(@PathVariable Long movieId, @AuthenticationPrincipal String userId) {
        boolean result = movieDetailService.isMyPick(movieId, Long.valueOf(userId));
        return ApiResponse.success(SuccessStatus.SEARCH_MYPICK_SUCCESS, result);
    }

    // 찜 등록
    @PostMapping("/mypick/{movieId}")
    public ResponseEntity<ApiResponse<Void>> addMyPick(@PathVariable Long movieId, @AuthenticationPrincipal String userId) {
        movieDetailService.addMyPick(movieId, Long.valueOf(userId));
        return ApiResponse.success_only(SuccessStatus.CREATE_MYPICK_SUCCESS);
    }

    // 찜 해제
    @DeleteMapping("/mypick/{movieId}")
    public ResponseEntity<ApiResponse<Void>> removeMyPick(@PathVariable Long movieId, @AuthenticationPrincipal String userId) {
        movieDetailService.removeMyPick(movieId, Long.valueOf(userId));
        return ApiResponse.success_only(SuccessStatus.DELETE_MYPICK_SUCCESS);
    }

    // 작품정보 탭
    @GetMapping("/{movieId}/info")
    public ResponseEntity<ApiResponse<MovieDetailDto>> getMovieInfo(@PathVariable Long movieId) {
        MovieDetailDto movieDetail = movieDetailService.getMovieDetail(movieId);
        return ApiResponse.success(SuccessStatus.SEARCH_MOVIE_DETAIL_SUCCESS, movieDetail);
    }

    // 한줄평 조회
    @GetMapping("/{movieId}/short-reviews")
    public ResponseEntity<ApiResponse<Page<ShortReviewResDto>>> getShortReviews
            (@PathVariable Long movieId, @RequestParam(defaultValue = "0") int page,
             @RequestParam(defaultValue = "20") int size, @AuthenticationPrincipal String userId) {
        Page<ShortReviewResDto> reviews = shortReviewService.getShortReviews(movieId, page, size, Long.valueOf(userId));
        return ApiResponse.success(SuccessStatus.SEARCH_SHORT_REVIEW_SUCCESS, reviews);
    }

    // 한줄평 등록
    @PostMapping("/{movieId}/short-reviews")
    public ResponseEntity<ApiResponse<Void>> createShortReview
    (@PathVariable Long movieId, @RequestBody ShortReviewReqDto shortReviewReqDto, @AuthenticationPrincipal String userId) {
        shortReviewService.createShortReview(movieId, shortReviewReqDto, Long.valueOf(userId));
        return ApiResponse.success_only(SuccessStatus.CREATE_SHORT_REVIEW_SUCCESS);
    }

    // 한줄평 수정
    @PutMapping("/{movieId}/short-reviews/{shortReviewId}")
    public ResponseEntity<ApiResponse<Void>> updateShortReview(@PathVariable Long movieId, @PathVariable Long shortReviewId,
            @RequestBody ShortReviewReqDto shortReviewReqDto, @AuthenticationPrincipal String userId) {
        shortReviewService.updateShortReview(movieId, shortReviewReqDto, shortReviewId, Long.valueOf(userId));
        return ApiResponse.success_only(SuccessStatus.UPDATE_SHORT_REVIEW_SUCCESS);
    }

    // 한줄평 삭제
    @DeleteMapping("/{movieId}/short-reviews/{shortReviewId}")
    public ResponseEntity<ApiResponse<Void>> deleteShortReview(@PathVariable Long movieId,
                                                               @PathVariable Long shortReviewId, @AuthenticationPrincipal String userId) {
        shortReviewService.deleteShortReview(shortReviewId, movieId, Long.valueOf(userId));
        return ApiResponse.success_only(SuccessStatus.DELETE_SHORT_REVIEW_SUCCESS);
    }

    // 신고
    @PostMapping("/report")
    public ResponseEntity<ApiResponse<Void>> report(@RequestBody ReportReqDto reportReqDto, @AuthenticationPrincipal String userId) {
        reviewService.report(reportReqDto, Long.valueOf(userId));
        return ApiResponse.success_only(SuccessStatus.REPORT_SUCCESS);
    }

    // 상세 리뷰 조회
    @GetMapping("/{movieId}/reviews")
    public ResponseEntity<ApiResponse<Page<ReviewResDto>>> getReviews(
            @PathVariable Long movieId, @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size, @AuthenticationPrincipal String userId) {
        log.info("========================== userId : " + userId + "==========================");
        Page<ReviewResDto> reviewPage = reviewService.getReviewList(movieId, page, size, Long.valueOf(userId));
        return ApiResponse.success(SuccessStatus.SEARCH_REVIEW_LIST_SUCCESS, reviewPage);
    }

    // 상세 리뷰 총 개수
    @GetMapping("/{movieId}/reviews/count")
    public ResponseEntity<ApiResponse<Long>> getReviewCount(@PathVariable Long movieId) {
        long count = reviewService.getReviewCount(movieId);
        return ApiResponse.success(SuccessStatus.SEARCH_REVIEW_COUNT_SUCCESS, count);
    }
}
