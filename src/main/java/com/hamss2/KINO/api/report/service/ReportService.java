package com.hamss2.KINO.api.report.service;

import com.hamss2.KINO.api.admin.repository.CommentRepository;
import com.hamss2.KINO.api.admin.repository.ReportRepository;
import com.hamss2.KINO.api.entity.Comment;
import com.hamss2.KINO.api.entity.Report;
import com.hamss2.KINO.api.entity.Review;
import com.hamss2.KINO.api.entity.User;
import com.hamss2.KINO.api.home.repository.ReviewRepository;
import com.hamss2.KINO.api.report.dto.ReportReqDto;
import com.hamss2.KINO.api.testPackage.UserRepository;
import com.hamss2.KINO.common.exception.BadRequestException;
import com.hamss2.KINO.common.exception.InternalServerException;
import com.hamss2.KINO.common.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final ReviewRepository reviewRepository;

    public Boolean reportReview(Long id, ReportReqDto reportReqDto) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new InternalServerException("사용자를 찾을 수 없습니다."));

        if (reportReqDto.getRelatedType() <= 0) {
            throw new BadRequestException("댓글 신고는 댓글이 달린 리뷰 게시글의 ID가 필요합니다.");
        }

        Review review = reviewRepository.findByReviewId(reportReqDto.getRelatedType())
            .orElseThrow(() -> new NotFoundException("신고된 댓글의 리뷰 게시글을 찾을 수 없습니다."));

        Comment comment = commentRepository.findByCommentId(reportReqDto.getRelatedId())
            .orElseThrow(() -> new NotFoundException("신고할 댓글을 찾을 수 없습니다."));

        User reporteeUser = comment.getUser();

        Report report = Report.createReport(
            reportReqDto.getContent(),
            reportReqDto.getReportType(),
            review.getReviewId(),
            comment.getCommentId(),
            user,
            reporteeUser
        );

        reportRepository.save(report);

        return true;
    }
}
