package com.hamss2.KINO.api.admin.repository;

import com.hamss2.KINO.api.entity.Comment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 댓글 조회
    // 리뷰 ID로 댓글 조회
    List<Comment> findByReviewReviewId(Long reviewId);

}
