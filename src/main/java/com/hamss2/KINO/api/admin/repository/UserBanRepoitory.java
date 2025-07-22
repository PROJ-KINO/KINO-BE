package com.hamss2.KINO.api.admin.repository;

import com.hamss2.KINO.api.entity.UserBan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserBanRepoitory extends JpaRepository<UserBan, Long> {
    // 월별 정지 회원수 카운트
    @Query("""
        SELECT FUNCTION('DATE_FORMAT', ub.createdAt, '%Y-%m') AS month, COUNT(DISTINCT ub.user.userId) AS banCount
        FROM UserBan ub
        WHERE ub.createdAt BETWEEN :start AND :end
        GROUP BY month
        ORDER BY month ASC
    """)
    List<Object[]> countBannedUsersByMonth(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
