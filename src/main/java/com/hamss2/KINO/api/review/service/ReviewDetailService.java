package com.hamss2.KINO.api.review.service;

import com.hamss2.KINO.api.entity.Movie;
import com.hamss2.KINO.api.entity.Review;
import com.hamss2.KINO.api.entity.Role;
import com.hamss2.KINO.api.entity.User;
import com.hamss2.KINO.api.home.repository.ReviewRepository;
import com.hamss2.KINO.api.movieAdmin.repository.MovieRepository;
import com.hamss2.KINO.api.review.dto.ReviewDetailResDto;
import com.hamss2.KINO.api.review.dto.WritingReviewResDto;
import com.hamss2.KINO.api.testPackage.UserRepository;
import com.hamss2.KINO.common.exception.NotFoundException;
import com.hamss2.KINO.common.utils.TimeFormatter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    public WritingReviewResDto startReview(Long userId, Long movieId) {

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

        WritingReviewResDto response = WritingReviewResDto.builder()
            .userId(user.getUserId())
            .userNickname(user.getNickname())
            .build();

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

        ReviewDetailResDto response = ReviewDetailResDto.builder()
            .reviewId(review.getReviewId())
            .reviewTitle(review.getTitle())
            .reviewContent(review.getContent())
            .reviewViewCount(review.getTotalViews())
            .reviewCommentCount(review.getComments().size())
            .reviewLikeCount(review.getReviewLikes().size())
            .reviewCreatedAt(TimeFormatter.formatLocalDateTime(review.getCreatedAt()))
            .movieId(movie.getMovieId())
            .movieTitle(movie.getTitle())
            .writerId(writer.getUserId())
            .writerUserNickname(writer.getNickname())
            .writerUserImage(writer.getImage())
            .isActive(user.getRole() != Role.BAN_USER)
            .isHeart(review.getReviewLikes().stream()
                .anyMatch(like -> like.getUser().getUserId().equals(user.getUserId())))
            .isMine(user.equals(writer))
            .build();

        // 리뷰 조회수 증가
        review.incrementViews();

        return response;
    }
}
