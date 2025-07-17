package com.hamss2.KINO.api.admin.service;

import com.hamss2.KINO.api.admin.dto.AdminReportCommentDetailResDto;
import com.hamss2.KINO.api.admin.dto.AdminReportReviewDetailResDto;
import com.hamss2.KINO.api.admin.dto.AdminReportShortReviewDetailResDto;
import com.hamss2.KINO.api.admin.dto.AdminReqDto;
import com.hamss2.KINO.api.admin.dto.AdminReviewResDto;
import com.hamss2.KINO.api.admin.dto.AdminUserResDto;
import com.hamss2.KINO.api.admin.repository.CommentRepository;
import com.hamss2.KINO.api.admin.repository.ReportRepository;
import com.hamss2.KINO.api.admin.repository.UserBanRepoitory;
import com.hamss2.KINO.api.entity.Comment;
import com.hamss2.KINO.api.entity.Report;
import com.hamss2.KINO.api.entity.Review;
import com.hamss2.KINO.api.entity.Role;
import com.hamss2.KINO.api.entity.ShortReview;
import com.hamss2.KINO.api.entity.UserBan;
import com.hamss2.KINO.api.home.repository.ReviewRepository;
import com.hamss2.KINO.api.movieDetail.repository.ShortReviewRepository;
import com.hamss2.KINO.api.testPackage.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminService {

    private final UserRepository userRepository;
    private final ReportRepository reportRepository;
    private final ShortReviewRepository shortReviewRepository;
    private final ReviewRepository reviewRepository;
    private final CommentRepository commentRepository;
    private final UserBanRepoitory userBanRepository;

    public Page<AdminUserResDto> getUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
            .map(u -> new AdminUserResDto(
                u.getUserId(),
                u.getNickname(),
                u.getEmail(),
                u.getRole().toString(),
                u.getCreatedAt()));
    }

    public Page<AdminReviewResDto> getReportShortReviews(Pageable pageable) {
        return reportRepository.findByRelatedType(-2, pageable)
            .map(r -> new AdminReviewResDto(
                r.getReportId(),
                r.getReporter().getEmail(),
                r.getReported().getEmail(),
                r.getReported().getRole().toString(),
                r.getCreatedAt()
            ));
    }

    public Page<AdminReviewResDto> getReportComments(Pageable pageable) {
        return reportRepository.findByRelatedTypeNotIn(List.of(-1, -2), pageable)
            .map(r -> new AdminReviewResDto(
                r.getReportId(),
                r.getReporter().getEmail(),
                r.getReported().getEmail(),
                r.getReported().getRole().toString(),
                r.getCreatedAt()
            ));
    }

    public Page<AdminReviewResDto> getReportReviews(Pageable pageable) {
        return reportRepository.findByRelatedType(-1, pageable)
            .map(r -> new AdminReviewResDto(
                r.getReportId(),
                r.getReporter().getEmail(),
                r.getReported().getEmail(),
                r.getReported().getRole().toString(),
                r.getCreatedAt()
            ));
    }

    public AdminReportShortReviewDetailResDto getReportShortReviewDetail(Long reportId) {
        Report report = reportRepository.findById(reportId)
            .orElseThrow(() -> new RuntimeException("Report not found"));
        ShortReview shortReview = shortReviewRepository.findById(report.getRelatedId())
            .orElseThrow(() -> new RuntimeException("ShortReview not found"));
        AdminReportShortReviewDetailResDto dto = new AdminReportShortReviewDetailResDto(
            report.getReported().getUserId(),
            report.getReportId(),
            report.getRelatedId(),
            report.getReportType(),
            report.getReporter().getEmail(),
            report.getReported().getEmail(),
            report.getCreatedAt(),
            report.getContent(),
            shortReview.getContent()
        );
        return dto;
    }

    public AdminReportCommentDetailResDto getReportCommentDetail(Long reportId) {
        Report report = reportRepository.findById(reportId)
            .orElseThrow(() -> new RuntimeException("Report not found"));
        Comment comment = commentRepository.findById(report.getRelatedId())
            .orElseThrow(() -> new RuntimeException("Comment not found"));
        AdminReportCommentDetailResDto dto = new AdminReportCommentDetailResDto(
            report.getReported().getUserId(),
            report.getReportId(),
            report.getRelatedId(),
            report.getRelatedType(),
            report.getReportType(),
            report.getReporter().getEmail(),
            report.getReported().getEmail(),
            report.getCreatedAt(),
            report.getContent(),
            comment.getContent()
        );
        return dto;
    }

    public AdminReportReviewDetailResDto getReportReviewDetail(Long reportId) {
        Report report = reportRepository.findById(reportId)
            .orElseThrow(() -> new RuntimeException("Report not found"));
        Review review = reviewRepository.findById(report.getRelatedId())
            .orElseThrow(() -> new RuntimeException("Review not found"));
        AdminReportReviewDetailResDto dto = new AdminReportReviewDetailResDto(
            report.getReported().getUserId(),
            report.getReportId(),
            report.getRelatedId(),
            report.getReportType(),
            report.getReporter().getEmail(),
            report.getReported().getEmail(),
            report.getCreatedAt(),
            report.getContent(),
            review.getContent(),
            review.getTitle()
        );
        return dto;
    }

    public void active(List<Long> userIds) {
        for (Long id : userIds) {
            userRepository.findById(id).ifPresent(user -> {
                user.setRole(Role.USER);
            });
            userBanRepository.deleteById(id);
        }
    }

    public void deleteReport(Long id) {
        reportRepository.deleteById(id);
    }

    public void process(AdminReqDto adminReqDto) {
        userRepository.findById(adminReqDto.getReportedId()).ifPresent(user -> {
            user.setRole(Role.BAN_USER);

            LocalDateTime bannedUntil = LocalDateTime.now()
                .plusDays(adminReqDto.getResult());

            UserBan userBan = new UserBan();
            userBan.setUser(user);
            userBan.setBannedUntil(bannedUntil);

            userBanRepository.save(userBan);
        });
    }

}
