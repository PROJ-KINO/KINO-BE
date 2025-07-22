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
    @Query(value = """
    SELECT 
      DATE_FORMAT(months.yyyymm, '%Y-%m') AS month,
      COUNT(DISTINCT ub.user_id) AS banCount
    FROM (
        SELECT DATE_FORMAT(DATE_ADD(:start, INTERVAL n MONTH), '%Y-%m-01') AS yyyymm
        FROM (
          SELECT 0 n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
          UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9
          UNION ALL SELECT 10 UNION ALL SELECT 11
        ) nums
        WHERE DATE_ADD(:start, INTERVAL n MONTH) <= :end
    ) months
    LEFT JOIN user_ban ub ON ub.created_at <= LAST_DAY(months.yyyymm) AND ub.banned_until >= months.yyyymm
    GROUP BY months.yyyymm
    ORDER BY months.yyyymm
    """, nativeQuery = true)
    List<Object[]> countBannedUsersByMonth(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
