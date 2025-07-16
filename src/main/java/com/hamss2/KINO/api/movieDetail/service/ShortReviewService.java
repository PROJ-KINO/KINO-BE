package com.hamss2.KINO.api.movieDetail.service;

import com.hamss2.KINO.api.admin.repository.ReportRepository;
import com.hamss2.KINO.api.entity.Movie;
import com.hamss2.KINO.api.entity.Report;
import com.hamss2.KINO.api.entity.ShortReview;
import com.hamss2.KINO.api.entity.User;
import com.hamss2.KINO.api.movieAdmin.repository.MovieRepository;
import com.hamss2.KINO.api.movieDetail.dto.req.ReportReqDto;
import com.hamss2.KINO.api.movieDetail.dto.req.ShortReviewReqDto;
import com.hamss2.KINO.api.movieDetail.dto.res.ShortReviewResDto;
import com.hamss2.KINO.api.movieDetail.repository.ShortReviewLikeRepository;
import com.hamss2.KINO.api.movieDetail.repository.ShortReviewRepository;
import com.hamss2.KINO.api.testPackage.UserRepository;
import com.hamss2.KINO.common.exception.BadRequestException;
import com.hamss2.KINO.common.exception.NotFoundException;
import com.hamss2.KINO.common.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShortReviewService {

    private final ShortReviewRepository shortReviewRepository;
    private final MovieRepository movieRepository;
    private final ShortReviewLikeRepository shortReviewLikeRepository;
    private final UserRepository userRepository;
    private final ReportRepository reportRepository;

    // 한줄평 리스트 조회(20개씩)
    @Transactional(readOnly = true)
    public Page<ShortReviewResDto> getShortReviews(Long movieId, int page, int size, Long userId) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 영화입니다."));
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ShortReview> reviews = shortReviewRepository.findAllByMovieAndIsDeletedFalseOrderByCreatedAtDesc(movie, pageable);

        // 좋아요 리뷰ID 목록 한 번에 미리 가져와서 N+1 방지
        Set<Long> myLikedReviewIds = shortReviewLikeRepository.findByUserUserId(userId)
                .stream().map(like -> like.getShortReview().getShortReviewId()).collect(Collectors.toSet());

        return reviews.map(review -> toDto(review, userId, myLikedReviewIds));
    }

    // 한줄평 등록
    @Transactional
    public void createShortReview(Long movieId, ShortReviewReqDto shortReviewReqDto, Long userId) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 영화입니다."));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 사용자입니다."));

        // 이미 작성한 한줄평이 있다면 예외
        Optional<ShortReview> alreadyReview = shortReviewRepository.findByUserAndMovieAndIsDeletedFalse(user, movie);
        if (alreadyReview.isPresent()) {
            throw new BadRequestException("이미 작성한 한줄평이 있습니다.");
        }

        ShortReview shortReview = new ShortReview();
        shortReview.setContent(shortReviewReqDto.getContent());
        shortReview.setRating(shortReviewReqDto.getRating());
        shortReview.setMovie(movie);
        shortReview.setUser(user);
        shortReview.setIsDeleted(false);
        shortReview.setIsActive(true);
        shortReview.setCreatedAt(LocalDateTime.now());
        shortReviewRepository.save(shortReview);
    }

    // 한줄평 수정
    @Transactional
    public void updateShortReview(Long movieId, ShortReviewReqDto shortReviewReqDto, Long shortReviewId, Long userId) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 영화입니다."));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 사용자입니다."));

        ShortReview shortReview = shortReviewRepository.findByShortReviewIdAndIsDeletedFalse(shortReviewId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 한줄평입니다."));

        // 본인만 수정 가능
        if (!shortReview.getUser().getUserId().equals(userId)) {
            throw new UnauthorizedException("내가 쓴 한줄평만 수정할 수 있습니다.");
        }

        if (!shortReview.getMovie().getMovieId().equals(movieId)) {
            throw new BadRequestException("영화 정보가 일치하지 않습니다.");
        }

        shortReview.setContent(shortReviewReqDto.getContent());
        shortReview.setRating(shortReviewReqDto.getRating());
        shortReviewRepository.save(shortReview);
    }

    // 한줄평 삭제
    @Transactional
    public void deleteShortReview(Long shortReviewId, Long movieId, Long userId) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 영화입니다."));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 사용자입니다."));

        ShortReview shortReview = shortReviewRepository.findByShortReviewIdAndIsDeletedFalse(shortReviewId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 한줄평입니다."));

        // 본인만 삭제 가능
        if (!shortReview.getUser().getUserId().equals(userId)) {
            throw new UnauthorizedException("내가 쓴 한줄평만 삭제할 수 있습니다.");
        }

        if (!shortReview.getMovie().getMovieId().equals(movieId)) {
            throw new BadRequestException("영화 정보가 일치하지 않습니다.");
        }

        // Soft delete 처리
        shortReview.setIsDeleted(true);
        shortReviewRepository.save(shortReview);
    }

    // 한줄평 신고
    @Transactional
    public void reportShortReview(ReportReqDto reportReqDto) {
        ShortReview review = shortReviewRepository.findByShortReviewIdAndIsDeletedFalse(reportReqDto.getRelatedId())
                .orElseThrow(() -> new NotFoundException("존재하지 않는 한줄평입니다."));
        User reporter = userRepository.findById(reportReqDto.getReporterId())
                .orElseThrow(() -> new NotFoundException("신고자가 존재하지 않습니다."));
        User reported = userRepository.findById(reportReqDto.getReporteeId())
                .orElseThrow(() -> new NotFoundException("피신고자가 존재하지 않습니다."));

        // 2. 중복 신고 방지(이미 신고한 내역 있는지)
        boolean alreadyReported = reportRepository.existsByReporterUserIdAndRelatedTypeAndRelatedId(
                reportReqDto.getReporterId(), reportReqDto.getRelatedType(), reportReqDto.getRelatedId());
        if (alreadyReported) {
            throw new BadRequestException("이미 신고한 한줄평입니다.");
        }

        // 3. 신고 엔티티 저장
        Report report = new Report();
        report.setReporter(reporter);
        report.setReported(reported);
        report.setReportType(reportReqDto.getReportType());  // 카테고리 번호 (ex. 욕설/광고/기타)
        report.setContent(reportReqDto.getContent());
        report.setRelatedType(reportReqDto.getRelatedType());   // ex) 1: ShortReview
        report.setRelatedId(reportReqDto.getRelatedId());
        report.setCreatedAt(LocalDateTime.now());

        reportRepository.save(report);
    }

    // 한줄평 DTO 변환 메서드
    private ShortReviewResDto toDto(ShortReview review, Long myUserId, Set<Long> myLikedReviewIds) {
        return ShortReviewResDto.builder()
                .shortReviewId(review.getShortReviewId())
                .userId(review.getUser().getUserId())
                .userNickname(review.getUser().getNickname())
                .userProfile(review.getUser().getImage())
                .rating(review.getRating())
                .content(review.getContent())
                .createdAt(review.getCreatedAt())
                .mine(review.getUser().getUserId().equals(myUserId))
                .likeCount(review.getShortReviewLikes() == null ? 0 : review.getShortReviewLikes().size())
                .liked(myLikedReviewIds != null && myLikedReviewIds.contains(review.getShortReviewId()))
                .build();
    }
}
