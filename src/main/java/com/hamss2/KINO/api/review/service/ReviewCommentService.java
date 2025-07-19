package com.hamss2.KINO.api.review.service;

import com.hamss2.KINO.api.admin.repository.CommentRepository;
import com.hamss2.KINO.api.entity.Comment;
import com.hamss2.KINO.api.entity.Review;
import com.hamss2.KINO.api.entity.Role;
import com.hamss2.KINO.api.entity.User;
import com.hamss2.KINO.api.home.repository.ReviewRepository;
import com.hamss2.KINO.api.review.dto.CommentReqDto;
import com.hamss2.KINO.api.review.dto.PageResDto;
import com.hamss2.KINO.api.review.dto.ReviewCommentResDto;
import com.hamss2.KINO.api.testPackage.UserRepository;
import com.hamss2.KINO.common.exception.BadRequestException;
import com.hamss2.KINO.common.exception.NotFoundException;
import com.hamss2.KINO.common.exception.UnauthorizedException;
import com.hamss2.KINO.common.reponse.ErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class ReviewCommentService {

    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final CommentRepository commentRepository;

    public PageResDto<ReviewCommentResDto> getCommentsByReviewId(
        Long userId,
        Long reviewId,
        int page,
        int size
    ) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

        Pageable pageable = PageRequest.of(page, size);

        Page<Comment> comments = commentRepository.findByReviewReviewIdOrderByCreatedAtDesc(
            reviewId, pageable);

        Page<ReviewCommentResDto> response = comments.map(comment -> {
            User writer = comment.getUser();

            return ReviewCommentResDto.builder()
                .commentId(comment.getCommentId())
                .commentContent(comment.getContent())
                .commentCreatedAt(comment.getCreatedAt())
                .isActive(comment.getIsActive())
                .writerId(writer.getUserId())
                .writerUserNickname(writer.getNickname())
                .writerUserImage(writer.getImage())
                .isMine(user.getUserId().equals(writer.getUserId()))
                .build();
        });

        return new PageResDto<>(response);
    }

    public ReviewCommentResDto createComment(Long userId, CommentReqDto commentReqDto) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));
        if (user.getRole().equals(Role.BAN_USER)) {
            throw new UnauthorizedException("사용이 정지된 사용자는 댓글 작성이 안됩니다.");
        }

        Review review = reviewRepository.findById(commentReqDto.getReviewId())
            .orElseThrow(() -> new NotFoundException(
                "Review not found with id: " + commentReqDto.getReviewId()));

        Comment comment = Comment.createComment(commentReqDto.getCommentContent(), user, review);
        commentRepository.save(comment);

        return ReviewCommentResDto.builder()
            .commentId(comment.getCommentId())
            .commentContent(comment.getContent())
            .commentCreatedAt(comment.getCreatedAt())
            .isActive(comment.getIsActive())
            .writerId(user.getUserId())
            .writerUserNickname(user.getNickname())
            .writerUserImage(user.getImage())
            .isMine(true)
            .build();
    }

    public Boolean deleteComment(Long userId, String commentId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

        Comment comment = commentRepository.findById(Long.parseLong(commentId))
            .orElseThrow(() -> new NotFoundException("Comment not found with id: " + commentId));

        if (comment.getIsDeleted()) {
            throw new BadRequestException(
                ErrorStatus.COMMENT_ALREADY_DELETED_EXCEPTION.getMessage());
        }

        if (!comment.getUser().getUserId().equals(userId)) {
            throw new UnauthorizedException("본인이 작성한 댓글만 삭제할 수 있습니다.");
        }

        comment.delete();

        return true;
    }
}
