package com.hamss2.KINO.api.admin.repository;

import com.hamss2.KINO.api.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findByRelatedType(int relatedTy);
    List<Report> findByRelatedTypeNotIn(List<Integer> excludedTypes);
}
