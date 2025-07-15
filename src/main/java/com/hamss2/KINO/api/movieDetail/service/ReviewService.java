package com.hamss2.KINO.api.movieDetail.service;

import com.hamss2.KINO.api.entity.Movie;
import com.hamss2.KINO.api.entity.Review;
import com.hamss2.KINO.api.home.repository.ReviewRepository;
import com.hamss2.KINO.api.movieAdmin.repository.MovieRepository;
import com.hamss2.KINO.api.movieDetail.dto.res.ReviewResDto;
import com.hamss2.KINO.common.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final MovieRepository movieRepository;

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
}
