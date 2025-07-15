package com.hamss2.KINO.api.admin.repository;

import com.hamss2.KINO.api.entity.ShortReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShortReviewRepository extends JpaRepository<ShortReview, Long> {
}
