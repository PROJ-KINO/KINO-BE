package com.hamss2.KINO.api.movieDetail.repository;

import com.hamss2.KINO.api.entity.Movie;
import com.hamss2.KINO.api.entity.ShortReview;
import com.hamss2.KINO.api.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ShortReviewRepository extends JpaRepository<ShortReview, Long> {
    // 영화별 한줄평 리스트 (페이지네이션)
    Page<ShortReview> findAllByMovieAndIsDeletedFalseOrderByCreatedAtDesc(Movie movie, Pageable pageable);
    // 특정 유저가 특정 영화에 쓴 한줄평(soft delete 제외, 중복방지/수정/삭제 용)
    Optional<ShortReview> findByUserAndMovieAndIsDeletedFalse(User user, Movie movie);
    // 한줄평 ID로 soft delete 아닌 것만 조회 (수정/삭제)
    Optional<ShortReview> findByShortReviewIdAndIsDeletedFalse(Long shortReviewId);
    @Query("""
    SELECT g.genreName, COUNT(sr.shortReviewId) as shortReviewCount
    FROM ShortReview sr JOIN sr.movie m JOIN MovieGenre mg ON mg.movie = m JOIN mg.genre g
    WHERE sr.isDeleted = false
    GROUP BY g.genreName
    ORDER BY shortReviewCount DESC
""")
    List<Object[]> findGenreByShortReviewCount();
}
