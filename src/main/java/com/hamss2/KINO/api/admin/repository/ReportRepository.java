package com.hamss2.KINO.api.admin.repository;

import com.hamss2.KINO.api.entity.Report;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    Page<Report> findByRelatedType(int relatedTy, Pageable pageable);
    Page<Report> findByRelatedTypeNotIn(List<Integer> excludedTypes, Pageable pageable);
    // 신고 중복 방지: 같은 사람이 같은 대상에 이미 신고했는지
    boolean existsByReporterUserIdAndRelatedTypeAndRelatedId(Long reporterId, int relatedType, Long relatedId);
}
