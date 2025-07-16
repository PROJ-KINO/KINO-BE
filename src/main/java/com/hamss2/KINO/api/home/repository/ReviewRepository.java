package com.hamss2.KINO.api.home.repository;

import com.hamss2.KINO.api.entity.Movie;
import com.hamss2.KINO.api.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    // 좋아요순 TOP 10 리뷰
    @Query("""
    SELECT r FROM Review r
    LEFT JOIN r.reviewLikes rl
    WHERE r.isDeleted = false
    GROUP BY r
    ORDER BY COUNT(rl) DESC
    """)
    List<Review> findTop10ByLikeCount();
    // 영화별 리뷰 리스트
    Page<Review> findAllByMovieAndIsDeletedFalseOrderByCreatedAtDesc(Movie movie, Pageable pageable);
    // 영화별 리뷰 총 개수
    long countByMovieAndIsDeletedFalse(Movie movie);
}
