package com.hamss2.KINO.api.movieDetail.repository;

import com.hamss2.KINO.api.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {
    // 신고 중복 방지: 같은 사람이 같은 대상에 이미 신고했는지
    boolean existsByReporterUserIdAndRelatedTypeAndRelatedId(Long reporterId, int relatedType, Long relatedId);
}
