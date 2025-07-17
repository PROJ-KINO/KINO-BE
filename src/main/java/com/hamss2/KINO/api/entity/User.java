package com.hamss2.KINO.api.entity;

import com.hamss2.KINO.api.auth.dto.SocialType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Table(
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_social_provider",
            columnNames = {"socialType", "providerId"})
    }
)
@Getter
@Setter
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
    @Enumerated(EnumType.STRING)
    private SocialType socialType;

    @Column(nullable = false)
    private String providerId;

    @Column(nullable = false)
    @ColumnDefault("false")
    private Boolean isDeleted = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.USER; // USER, ADMIN, BAN_USER

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    @ColumnDefault("true")
    private Boolean isFirstLogin = true;

    @OneToMany(mappedBy = "follower", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Follow> following;

    @OneToMany(mappedBy = "followee", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Follow> followers;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserBan> userBans;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserGenre> userGenres;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ShortReview> shortReviews;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ShortReviewLike> shortReviewLikes;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReviewLike> reviewLikes;

    @OneToMany(mappedBy = "reporter", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Report> reports;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MyPickMovie> myPickMovies;

    public static User createUser(
        String nickname,
        String email,
        SocialType socialType,
        String providerId,
        String image
    ) {
        User user = new User();
        user.nickname = nickname;
        user.email = email;
        user.socialType = socialType;
        user.providerId = providerId;
        user.image = image;
        user.role = Role.USER;
        return user;
    }

    @PrePersist
    private void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
