package com.hamss2.KINO.api.movieAdmin.repository;

import com.hamss2.KINO.api.entity.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MovieRepository extends JpaRepository<Movie, Long> {
    // plot이 null이 아니고 빈 문자열이 아닌, teaserUrl이 있는, releaseDate 최신 영화 1개
    Movie findFirstByTeaserUrlIsNotNullAndPlotIsNotNullAndPlotNotOrderByReleaseDateDesc(String plot);

    List<Movie> findByTitleContaining(String keyword);
    List<Movie> findAllByTitle(String title);

    Page<Movie> findByMovieIdIn(List<Long> ids, Pageable pageable);
    @Query("SELECT m FROM Movie m WHERE REPLACE(m.title, ' ', '') LIKE %:title%")
    List<Movie> findAllByTitleIgnoringSpace(@Param("title") String title);


    @Query("SELECT DISTINCT m FROM Movie m JOIN FETCH m.movieGenres mg JOIN FETCH mg.genre")
    List<Movie> findAllWithGenres();
}
