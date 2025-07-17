package com.hamss2.KINO.api.movieDetail.service;

import com.hamss2.KINO.api.admin.repository.CommentRepository;
import com.hamss2.KINO.api.admin.repository.ReportRepository;
import com.hamss2.KINO.api.entity.Movie;
import com.hamss2.KINO.api.entity.Report;
import com.hamss2.KINO.api.entity.Review;
import com.hamss2.KINO.api.entity.User;
import com.hamss2.KINO.api.home.repository.ReviewRepository;
import com.hamss2.KINO.api.movieAdmin.repository.MovieRepository;
import com.hamss2.KINO.api.movieDetail.dto.req.ReportReqDto;
import com.hamss2.KINO.api.movieDetail.dto.res.ReviewResDto;
import com.hamss2.KINO.api.movieDetail.repository.ShortReviewRepository;
import com.hamss2.KINO.api.testPackage.UserRepository;
import com.hamss2.KINO.common.exception.BadRequestException;
import com.hamss2.KINO.common.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final MovieRepository movieRepository;
    private final CommentRepository commentRepository;
    private final ShortReviewRepository shortReviewRepository;
    private final ReportRepository reportRepository;
    private final UserRepository userRepository;


    // 리뷰 목록
    @Transactional(readOnly = true)
    public Page<ReviewResDto> getReviewList(Long movieId, int page, int size) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 영화입니다."));
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Review> reviewPage = reviewRepository.findAllByMovieAndIsDeletedFalseOrderByCreatedAtDesc(movie, pageable);

        return reviewPage.map(r -> ReviewResDto.builder()
                .reviewId(r.getReviewId())
                .userNickname(r.getUser().getNickname())
                .userProfile(r.getUser().getImage())
                .title(r.getTitle())
                .content(r.getContent())
                .totalViews(r.getTotalViews() != null ? r.getTotalViews() : 0)
                .commentCount(r.getComments() != null ? r.getComments().size() : 0)
                .createdAt(r.getCreatedAt())
                .build()
        );
    }

    // 리뷰 총 개수
    @Transactional(readOnly = true)
    public long getReviewCount(Long movieId) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 영화입니다."));
        return reviewRepository.countByMovieAndIsDeletedFalse(movie);
    }

    @Transactional
    public void report(ReportReqDto reportReqDto) {
        User reporter = userRepository.findById(reportReqDto.getReporterId())
                .orElseThrow(() -> new NotFoundException("신고자가 존재하지 않습니다."));
        User reportee = userRepository.findById(reportReqDto.getReporteeId())
                .orElseThrow(() -> new NotFoundException("피신고자가 존재하지 않습니다."));

        // 신고 대상 검증
        if (reportReqDto.getRelatedType() == -1) {
            // 한줄평
            if (!shortReviewRepository.existsById(reportReqDto.getRelatedId()))
                throw new NotFoundException("존재하지 않는 한줄평입니다.");
        } else if (reportReqDto.getRelatedType() == -2) {
            // 상세리뷰
            if (!reviewRepository.existsById(reportReqDto.getRelatedId()))
                throw new NotFoundException("존재하지 않는 상세리뷰입니다.");
        } else {
            // 댓글: relatedType에 상세리뷰 Id, relatedId에 댓글Id
            // Id에 해당하는 상세리뷰가 있는지 검증
            if (!reviewRepository.existsById((long) reportReqDto.getRelatedType()))
                throw new NotFoundException("존재하지 않는 상세리뷰(댓글의 상위글)입니다.");
            // 댓글ID에 해당하는 댓글이 있는지 검증
            if (!commentRepository.existsById(reportReqDto.getRelatedId()))
                throw new NotFoundException("존재하지 않는 댓글입니다.");
        }

        // 중복 신고 방지
        boolean already = reportRepository.existsByReporterUserIdAndRelatedTypeAndRelatedId(
                reportReqDto.getReporterId(), reportReqDto.getRelatedType(), reportReqDto.getRelatedId());
        if (already) throw new BadRequestException("이미 신고한 대상입니다.");

        // 신고 저장
        Report report = new Report();
        report.setReporter(reporter);
        report.setReported(reportee);
        report.setReportType(reportReqDto.getReportType());
        report.setContent(reportReqDto.getContent());
        report.setRelatedType(reportReqDto.getRelatedType());
        report.setRelatedId(reportReqDto.getRelatedId());
        report.setCreatedAt(LocalDateTime.now());
        reportRepository.save(report);
    }
}
