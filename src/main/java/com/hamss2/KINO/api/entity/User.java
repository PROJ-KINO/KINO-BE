package com.hamss2.KINO.api.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(nullable = false)
    private String nickname;

    private String image;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String socialType;

    @Column(nullable = false)
    private String providerId;

    @Column(nullable = false)
    @ColumnDefault("false")
    private Boolean isDeleted;

    @Column(nullable = false)
    private String role;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "follower", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Follow> following;

    @OneToMany(mappedBy = "followee", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Follow> followers;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UserBan> userBans;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UserGenre> userGenres;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Review> reviews;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Comment> comments;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ShortReview> shortReviews;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ShortReviewLike> shortReviewLikes;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ReviewLike> reviewLikes;

    @OneToMany(mappedBy = "reporter", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Report> reports;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MyPickMoive> myPickMovies;

    @PrePersist
    private void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
