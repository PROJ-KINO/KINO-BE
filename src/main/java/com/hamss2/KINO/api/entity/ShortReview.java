package com.hamss2.KINO.api.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.ColumnDefault;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class ShortReview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long shortReviewId;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private int rating = 0;

    @Column(nullable = false)
    @ColumnDefault("false")
    private Boolean isDeleted;

    @Column(nullable = false)
    @ColumnDefault("true")
    private Boolean isActive;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", nullable = false)
    @JsonIgnore
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movieId", nullable = false)
    @JsonIgnore
    private Movie movie;

    @OneToMany(mappedBy = "shortReview", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ShortReviewLike> shortReviewLikes;

    @PrePersist
    private void prePersist() {
        createdAt = LocalDateTime.now();
    }
} 