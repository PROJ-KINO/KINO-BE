package com.hamss2.KINO.api.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.ColumnDefault;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class DailyMovieView {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long dailyViewId;

    @Column(nullable = false)
    @ColumnDefault("0")
    private Integer dailyView;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movieId", nullable = false)
    @JsonIgnore
    private Movie movie;

    @PrePersist
    private void prePersist() {
        createdAt = LocalDateTime.now();
    }
} 