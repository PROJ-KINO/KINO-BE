package com.hamss2.KINO.api.review.service;

import com.hamss2.KINO.api.entity.Movie;
import com.hamss2.KINO.api.entity.User;
import com.hamss2.KINO.api.home.repository.ReviewRepository;
import com.hamss2.KINO.api.movieAdmin.repository.MovieRepository;
import com.hamss2.KINO.api.review.dto.WritingReviewResDto;
import com.hamss2.KINO.api.testPackage.UserRepository;
import com.hamss2.KINO.common.exception.NotFoundException;
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
}
