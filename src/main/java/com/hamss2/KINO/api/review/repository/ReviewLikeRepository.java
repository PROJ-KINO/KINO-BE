package com.hamss2.KINO.api.review.repository;

import com.hamss2.KINO.api.entity.ReviewLike;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewLikeRepository extends JpaRepository<ReviewLike, Long> {

    Optional<ReviewLike> findByUserUserIdAndReviewReviewId(Long userId, Long reviewId);
}

