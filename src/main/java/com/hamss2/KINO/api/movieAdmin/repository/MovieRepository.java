package com.hamss2.KINO.api.movieAdmin.repository;

import com.hamss2.KINO.api.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MovieRepository extends JpaRepository<Movie, Long> {
    // 티저 영상이 있는 최신 영화 1개
    Movie findFirstByTeaserUrlIsNotNullOrderByReleaseDateDesc();
    List<Movie> findByTitleContaining(String keyword);
    List<Movie> findAllByTitle(String title);
}
