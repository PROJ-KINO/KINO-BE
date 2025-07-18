package com.hamss2.KINO.api.movieAdmin.repository;

import com.hamss2.KINO.api.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MovieRepository extends JpaRepository<Movie, Long> {
    // 티저 영상이 있는 최신 영화 1개
    Movie findFirstByTeaserUrlIsNotNullOrderByReleaseDateDesc();
    List<Movie> findByTitleContaining(String keyword);
    List<Movie> findAllByTitle(String title);


    @Query("SELECT DISTINCT m FROM Movie m JOIN FETCH m.movieGenres mg JOIN FETCH mg.genre")
    List<Movie> findAllWithGenres();
}
