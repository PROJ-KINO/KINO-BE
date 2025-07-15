package com.hamss2.KINO.api.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class ShortReviewLike {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long shortLikeId;

    @Column(nullable = false)
    private int likeCount = 0;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", nullable = false)
    @JsonIgnore
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shortReviewId", nullable = false)
    @JsonIgnore
    private ShortReview shortReview;

    @PrePersist
    private void prePersist() {
        createdAt = LocalDateTime.now();
    }
} 