package com.hamss2.KINO.api.movieDetail.service;

import com.hamss2.KINO.api.admin.repository.CommentRepository;
import com.hamss2.KINO.api.admin.repository.ReportRepository;
import com.hamss2.KINO.api.entity.*;
import com.hamss2.KINO.api.home.repository.ReviewRepository;
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
    public ShortReviewResDto createShortReview(Long movieId, ShortReviewReqDto shortReviewReqDto, Long userId) {
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

        // 좋아요 리뷰ID 목록 한 번에 미리 가져와서 N+1 방지
        Set<Long> myLikedReviewIds = shortReviewLikeRepository.findByUserUserId(userId)
                .stream().map(like -> like.getShortReview().getShortReviewId()).collect(Collectors.toSet());

        return toDto(shortReview, userId, myLikedReviewIds);
    }

    // 한줄평 수정
    @Transactional
    public ShortReviewResDto updateShortReview(Long movieId, ShortReviewReqDto shortReviewReqDto, Long shortReviewId, Long userId) {
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
        shortReview.setCreatedAt(LocalDateTime.now());
        shortReviewRepository.save(shortReview);

        // 좋아요 리뷰ID 목록 한 번에 미리 가져와서 N+1 방지
        Set<Long> myLikedReviewIds = shortReviewLikeRepository.findByUserUserId(userId)
                .stream().map(like -> like.getShortReview().getShortReviewId()).collect(Collectors.toSet());

        return toDto(shortReview, userId, myLikedReviewIds);
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

    @Transactional
    public boolean toggleLike(Long shortReviewId, Long userId) {
        ShortReview shortReview = shortReviewRepository.findById(shortReviewId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 한줄평입니다."));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 사용자입니다."));

        Optional<ShortReviewLike> like = shortReviewLikeRepository.findByUserAndShortReview(user, shortReview);
        if (like.isPresent()) {
            // 좋아요 취소
            shortReviewLikeRepository.delete(like.get());
            return false;
        } else {
            // 좋아요 등록
            ShortReviewLike newLike = new ShortReviewLike();
            newLike.setShortReview(shortReview);
            newLike.setUser(user);
            newLike.setCreatedAt(LocalDateTime.now());
            shortReviewLikeRepository.save(newLike);
            return true;
        }
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
