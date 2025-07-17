package com.hamss2.KINO.api.movieDetail.controller;

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
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MovieDetailController {

    private final MovieDetailService movieDetailService;
    private final ShortReviewService shortReviewService;
    private final ReviewService reviewService;

    // 찜 여부 확인
    @GetMapping("/mypick/{movieId}")
    public ResponseEntity<ApiResponse<Boolean>> isMyPick(@PathVariable Long movieId, Long userId) {
        boolean result = movieDetailService.isMyPick(movieId, userId);
        return ApiResponse.success(SuccessStatus.SEARCH_MYPICK_SUCCESS, result);
    }

    // 찜 등록
    @PostMapping("/mypick/{movieId}")
    public ResponseEntity<ApiResponse<Void>> addMyPick(@PathVariable Long movieId, Long userId) {
        movieDetailService.addMyPick(movieId, userId);
        return ApiResponse.success_only(SuccessStatus.CREATE_MYPICK_SUCCESS);
    }

    // 찜 해제
    @DeleteMapping("/mypick/{movieId}")
    public ResponseEntity<ApiResponse<Void>> removeMyPick(@PathVariable Long movieId, Long userId) {
        movieDetailService.removeMyPick(movieId, userId);
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
             @RequestParam(defaultValue = "20") int size, Long userId) {
        Page<ShortReviewResDto> reviews = shortReviewService.getShortReviews(movieId, page, size, userId);
        return ApiResponse.success(SuccessStatus.SEARCH_SHORT_REVIEW_SUCCESS, reviews);
    }

    // 한줄평 등록
    @PostMapping("/{movieId}/short-reviews")
    public ResponseEntity<ApiResponse<Void>> createShortReview
    (@PathVariable Long movieId, @RequestBody ShortReviewReqDto shortReviewReqDto, @RequestParam Long userId) {
        shortReviewService.createShortReview(movieId, shortReviewReqDto, userId);
        return ApiResponse.success_only(SuccessStatus.CREATE_SHORT_REVIEW_SUCCESS);
    }

    // 한줄평 수정
    @PutMapping("/{movieId}/short-reviews/{shortReviewId}")
    public ResponseEntity<ApiResponse<Void>> updateShortReview(@PathVariable Long movieId, @PathVariable Long shortReviewId,
            @RequestBody ShortReviewReqDto shortReviewReqDto, @RequestParam Long userId) {
        shortReviewService.updateShortReview(movieId, shortReviewReqDto, shortReviewId, userId);
        return ApiResponse.success_only(SuccessStatus.UPDATE_SHORT_REVIEW_SUCCESS);
    }

    // 한줄평 삭제
    @DeleteMapping("/{movieId}/short-reviews/{shortReviewId}")
    public ResponseEntity<ApiResponse<Void>> deleteShortReview(@PathVariable Long movieId,
                                                               @PathVariable Long shortReviewId, @RequestParam Long userId) {
        shortReviewService.deleteShortReview(shortReviewId, movieId, userId);
        return ApiResponse.success_only(SuccessStatus.DELETE_SHORT_REVIEW_SUCCESS);
    }

    // 한줄평 신고
    @PostMapping("/report")
    public ResponseEntity<ApiResponse<Void>> report(@RequestBody ReportReqDto reportReqDto) {
        reviewService.report(reportReqDto);
        return ApiResponse.success_only(SuccessStatus.REPORT_SUCCESS);
    }

    // 상세 리뷰 조회
    @GetMapping("/{movieId}/reviews")
    public ResponseEntity<ApiResponse<Page<ReviewResDto>>> getReviews(
            @PathVariable Long movieId, @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<ReviewResDto> reviewPage = reviewService.getReviewList(movieId, page, size);
        return ApiResponse.success(SuccessStatus.SEARCH_REVIEW_LIST_SUCCESS, reviewPage);
    }

    // 상세 리뷰 총 개수
    @GetMapping("/{movieId}/reviews/count")
    public ResponseEntity<ApiResponse<Long>> getReviewCount(@PathVariable Long movieId) {
        long count = reviewService.getReviewCount(movieId);
        return ApiResponse.success(SuccessStatus.SEARCH_REVIEW_COUNT_SUCCESS, count);
    }
}
