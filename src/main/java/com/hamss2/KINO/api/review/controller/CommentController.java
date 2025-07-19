package com.hamss2.KINO.api.review.controller;


import com.hamss2.KINO.api.review.dto.CommentReqDto;
import com.hamss2.KINO.api.review.dto.PageResDto;
import com.hamss2.KINO.api.review.dto.ReviewCommentResDto;
import com.hamss2.KINO.api.review.service.ReviewCommentService;
import com.hamss2.KINO.common.exception.BadRequestException;
import com.hamss2.KINO.common.reponse.ApiResponse;
import com.hamss2.KINO.common.reponse.SuccessStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/api/comment")
@RequiredArgsConstructor
public class CommentController {

    private final ReviewCommentService reviewCommentService;

    @GetMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<PageResDto<ReviewCommentResDto>>> getComments(
        @AuthenticationPrincipal String userId,
        @PathVariable Long reviewId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "30") int size
    ) {
        if (userId == null || userId.isEmpty()) {
            throw new BadRequestException("userId is required");
        }
        Long id = Long.parseLong(userId);
        return ApiResponse.success(
            SuccessStatus.REVIEW_COMMENT_SUCCESS,
            reviewCommentService.getCommentsByReviewId(id, reviewId, page, size)
        );
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ReviewCommentResDto>> createComment(
        @AuthenticationPrincipal String userId, @RequestBody CommentReqDto commentReqDto) {
        if (userId == null || userId.isEmpty()) {
            throw new BadRequestException("userId is required");
        }
        Long id = Long.parseLong(userId);
        return ApiResponse.success(SuccessStatus.CREATE_REVIEW_COMMENT_SUCCESS,
            reviewCommentService.createComment(id, commentReqDto));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<ApiResponse<Boolean>> deleteComment(
        @AuthenticationPrincipal String userId, @PathVariable String commentId) {
        if (userId == null || userId.isEmpty()) {
            throw new BadRequestException("userId is required");
        }
        Long id = Long.parseLong(userId);
        return ApiResponse.success(SuccessStatus.DELETE_REVIEW_COMMENT_SUCCESS,
            reviewCommentService.deleteComment(id, commentId));
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<ApiResponse<ReviewCommentResDto>> updateComment(
        @AuthenticationPrincipal String userId,
        @PathVariable Long commentId,
        @RequestBody CommentReqDto commentReqDto
    ) {
        if (userId == null || userId.isEmpty()) {
            throw new BadRequestException("userId is required");
        }
        Long id = Long.parseLong(userId);
        return ApiResponse.success(SuccessStatus.UPDATE_REVIEW_COMMENT_SUCCESS,
            reviewCommentService.updateComment(id, commentId, commentReqDto));
    }

}
