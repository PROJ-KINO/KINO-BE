package com.hamss2.KINO.api.review.service;

import com.hamss2.KINO.api.admin.repository.CommentRepository;
import com.hamss2.KINO.api.entity.Comment;
import com.hamss2.KINO.api.entity.User;
import com.hamss2.KINO.api.review.dto.ReviewCommentResDto;
import com.hamss2.KINO.api.testPackage.UserRepository;
import com.hamss2.KINO.common.exception.NotFoundException;
import com.hamss2.KINO.common.utils.TimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class ReviewCommentService {

    private final UserRepository userRepository;
    private final CommentRepository commentRepository;


    public List<ReviewCommentResDto> getCommentsByReviewId(Long id, Long reviewId) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("User not found with id: " + id));
        List<Comment> comments = commentRepository.findByReviewReviewId(reviewId);

        return comments.stream().map(comment -> {
            User writer = comment.getUser();

            return ReviewCommentResDto.builder()
                .commentId(comment.getCommentId())
                .commentContent(comment.getContent())
                .commentCreatedAt(TimeFormatter.formatLocalDateTime(comment.getCreatedAt()))
                .isActive(comment.getIsActive())
                .writerId(writer.getUserId())
                .writerUserNickname(writer.getNickname())
                .writerUserImage(writer.getImage())
                .isMine(user.getUserId().equals(writer.getUserId()))
                .build();
        }).toList();
    }
}
