package com.hamss2.KINO.api.movieDetail.repository;

import com.hamss2.KINO.api.entity.ShortReviewLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShortReviewLikeRepository extends JpaRepository<ShortReviewLike, Long> {
    // 한 유저가 좋아요한 모든 한줄평
    List<ShortReviewLike> findByUserUserId(Long userId);
}
