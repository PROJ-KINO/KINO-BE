package com.hamss2.KINO.api.review.controller;


import com.hamss2.KINO.api.review.dto.ReviewCommentResDto;
import com.hamss2.KINO.api.review.service.ReviewCommentService;
import com.hamss2.KINO.common.exception.BadRequestException;
import com.hamss2.KINO.common.reponse.ApiResponse;
import com.hamss2.KINO.common.reponse.SuccessStatus;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/api/comment")
@RequiredArgsConstructor
public class CommentController {

    private final ReviewCommentService reviewCommentService;

    @GetMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<List<ReviewCommentResDto>>> getComments(
        @AuthenticationPrincipal String userId,
        @PathVariable Long reviewId
    ) {
        if (userId == null || userId.isEmpty()) {
            throw new BadRequestException("userId is required");
        }
        Long id = Long.parseLong(userId);
        List<ReviewCommentResDto> comments = reviewCommentService.getCommentsByReviewId(id,
            reviewId);
        return ApiResponse.success(SuccessStatus.REVIEW_COMMENT_SUCCESS, comments);
    }

}
