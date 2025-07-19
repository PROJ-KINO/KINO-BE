package com.hamss2.KINO.api.review.service;

import com.hamss2.KINO.api.entity.Movie;
import com.hamss2.KINO.api.entity.Review;
import com.hamss2.KINO.api.entity.ReviewLike;
import com.hamss2.KINO.api.entity.Role;
import com.hamss2.KINO.api.entity.User;
import com.hamss2.KINO.api.home.repository.ReviewRepository;
import com.hamss2.KINO.api.movieAdmin.repository.MovieRepository;
import com.hamss2.KINO.api.review.dto.PageResDto;
import com.hamss2.KINO.api.review.dto.ReviewDetailResDto;
import com.hamss2.KINO.api.review.dto.ReviewReqDto;
import com.hamss2.KINO.api.review.dto.ReviewResDto;
import com.hamss2.KINO.api.review.dto.ReviewUpdateReqDto;
import com.hamss2.KINO.api.review.dto.WritingReviewResDto;
import com.hamss2.KINO.api.review.repository.ReviewLikeRepository;
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
public class ReviewDetailService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final MovieRepository movieRepository;
    private final ReviewLikeRepository reviewLikeRepository;

    public WritingReviewResDto startReview(Long userId, Long movieId) {

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

        WritingReviewResDto response = WritingReviewResDto.builder().userId(user.getUserId())
            .userNickname(user.getNickname()).build();

        if (movieId != null) {
            Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new NotFoundException("Movie not found with id: " + movieId));

            response.setMovieId(movie.getMovieId());
            response.setMovieTitle(movie.getTitle());
            response.setMoviePosterUrl(movie.getPosterUrl());
            response.setMovieReleaseDate(movie.getReleaseDate().toString());
            response.setMovieAverageRating(movie.getAvgRating());
        }

        return response;

    }

    public ReviewDetailResDto getReviewDetail(Long id, Long reviewId) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("User not found with id: " + id));
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new NotFoundException("User not found with id: " + reviewId));
        Movie movie = review.getMovie();
        User writer = review.getUser();

        ReviewDetailResDto response = ReviewDetailResDto.builder().reviewId(review.getReviewId())
            .reviewTitle(review.getTitle()).reviewContent(review.getContent())
            .reviewViewCount(review.getTotalViews()).reviewCommentCount(review.getComments().size())
            .reviewLikeCount(review.getReviewLikes().size())
            .reviewCreatedAt(review.getCreatedAt())
            .movieId(movie.getMovieId()).movieTitle(movie.getTitle()).writerId(writer.getUserId())
            .writerUserNickname(writer.getNickname()).writerUserImage(writer.getImage())
            .isActive(user.getRole() != Role.BAN_USER).isHeart(review.getReviewLikes().stream()
                .anyMatch(like -> like.getUser().getUserId().equals(user.getUserId())))
            .isMine(user.equals(writer)).build();

        // 리뷰 조회수 증가
        review.incrementViews();

        return response;
    }

    public Boolean createReview(Long userId, ReviewReqDto reviewReqDto) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

        if (user.getRole() == Role.BAN_USER) {
            throw new UnauthorizedException("Only Not Ban Users can create review");
        }

        Movie movie = movieRepository.findById(reviewReqDto.getMovieId()).orElseThrow(
            () -> new NotFoundException("Movie not found with id: " + reviewReqDto.getMovieId()));

        Review review = Review.createReview(reviewReqDto.getReviewTitle(),
            reviewReqDto.getReviewContent(), user, movie);

        reviewRepository.save(review);

        return true;
    }

    public PageResDto<ReviewResDto> getReviews(Long userId, int page, int size) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

        Pageable pageable = PageRequest.of(page, size);

        Page<Review> reviews = reviewRepository.findAllByOrderByCreatedAtDesc(pageable);

        Page<ReviewResDto> response = reviews.map(review -> {
            return ReviewResDto.builder()
                .reviewId(review.getReviewId())
                .title(review.getTitle())
                .content(review.getContent())
                .viewCount(review.getTotalViews())
                .createdAt(review.getCreatedAt())
                .commentCount(review.getComments().size())
                .likeCount(review.getReviewLikes().size())
                .isMine(review.getUser().getUserId().equals(user.getUserId()))
                .isHeart(review.getReviewLikes().stream()
                    .anyMatch(like -> like.getUser().getUserId().equals(user.getUserId())))
                .build();
        });

        return new PageResDto<>(response);
    }

    public Boolean deleteReview(Long userId, Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new NotFoundException("Review not found with id: " + reviewId));

        if (review.getIsDeleted()) {
            throw new BadRequestException(
                ErrorStatus.REVIEW_ALREADY_DELETED_EXCEPTION.getMessage());
        }

        if (!review.getUser().getUserId().equals(userId)) {
            throw new UnauthorizedException(
                ErrorStatus.NOT_ALLOWED_OTHERS_REVIEW_EXCEPTION.getMessage());
        }

        review.deleteReview();

        return true;
    }

    public Boolean updateLike(Long userId, Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new NotFoundException("Review not found with id: " + reviewId));

        if (review.getIsDeleted()) {
            throw new BadRequestException(
                ErrorStatus.REVIEW_ALREADY_DELETED_EXCEPTION.getMessage());
        }

//        if (review.getIsActive()) {
//            throw new InternalServerException("Review is already active, cannot update.");
//        }

        ReviewLike reviewLike = review.getReviewLikes().stream()
            .filter(like -> like.getUser().getUserId().equals(userId))
            .findFirst().orElse(null);

        reviewLikeRepository.findByUserUserIdAndReviewReviewId(userId, reviewId)
            .ifPresentOrElse(like -> {
                // 이미 좋아요가 눌려있다면 좋아요 취소
                reviewLikeRepository.delete(like);
            }, () -> {
                // 좋아요가 눌려있지 않다면 좋아요 추가
                User user = userRepository.findById(userId)
                    .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));
                ReviewLike newLike = ReviewLike.createReviewLike(user, review);
                reviewLikeRepository.save(newLike);
            });

        return true;

    }

    public Long updateReview(Long userId, ReviewUpdateReqDto reviewReqDto) {

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

        Review review = reviewRepository.findById(reviewReqDto.getReviewId())
            .orElseThrow(() -> new NotFoundException(
                "Review not found with id: " + reviewReqDto.getReviewId()));

        if (review.getIsDeleted()) { // if(review.getIsDeleted() || !review.getIsActive()) {
            throw new BadRequestException(
                ErrorStatus.REVIEW_ALREADY_DELETED_EXCEPTION.getMessage());
        }

        if (!review.getUser().getUserId().equals(userId)) {
            throw new UnauthorizedException(
                ErrorStatus.NOT_ALLOWED_OTHERS_REVIEW_EXCEPTION.getMessage());
        }

        Movie movie = movieRepository.findById(reviewReqDto.getMovieId())
            .orElseThrow(() -> new NotFoundException(
                "Movie not found with id: " + reviewReqDto.getMovieId()));

        review.setTitle(reviewReqDto.getReviewTitle());
        review.setContent(reviewReqDto.getReviewContent());
        review.setMovie(movie);

        return review.getReviewId();
    }
}
