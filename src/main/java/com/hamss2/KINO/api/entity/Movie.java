package com.hamss2.KINO.api.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class Movie {

    @Id
    private Long movieId;

    @Column(nullable = false)
    private String title;

    private String posterUrl;

    private String teaserUrl;

    @Column(nullable = false)
    private LocalDate releaseDate;

    private String avgRating;

    @Lob
    @Column(nullable = false)
    private String plot = "";

    @Column(nullable = false)
    private Integer totalView;

    private String director;

    private String actors;

    private Integer runningTime;

    @Column(nullable = false)
    @ColumnDefault("false")
    private Boolean isDeleted = false;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MovieGenre> movieGenres;

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Review> reviews;

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ShortReview> shortReviews;

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DailyMovieView> dailyMovieViews;

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MyPickMovie> myPickMovies;

    @PrePersist
    private void prePersist() {
        createdAt = LocalDateTime.now();
    }
} 