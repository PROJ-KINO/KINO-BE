package com.hamss2.KINO.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reportId;

    @Column(nullable = false)
    private String content;

    private Integer reportType;

    private Long relatedType;

    private Long relatedId;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporterId", nullable = false)
    @JsonIgnore
    private User reporter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporteeId", nullable = false)
    @JsonIgnore
    private User reported;

    public static Report createReport(
        String content,
        int reportType,
        Long relatedType,
        Long relatedId,
        User reporter,
        User reported
    ) {
        Report report = new Report();
        report.setContent(content);
        report.setReportType(reportType);
        report.setRelatedType(relatedType);
        report.setRelatedId(relatedId);
        report.setReporter(reporter);
        report.setReported(reported);
        return report;
    }

    @PrePersist
    private void prePersist() {
        createdAt = LocalDateTime.now();
    }
} 