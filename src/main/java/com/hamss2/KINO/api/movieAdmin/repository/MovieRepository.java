package com.hamss2.KINO.api.movieAdmin.repository;

import com.hamss2.KINO.api.entity.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MovieRepository extends JpaRepository<Movie, Long> {
    // plot이 null이 아니고 빈 문자열이 아닌, 현재 개봉한, teaserUrl이 있는, releaseDate 최신 영화 1개
    Movie findFirstByTeaserUrlIsNotNullAndPlotIsNotNullAndPlotNotAndReleaseDateLessThanEqualOrderByReleaseDateDesc(String plot, LocalDate today);

    @Query("SELECT DISTINCT m FROM Movie m LEFT JOIN FETCH m.movieGenres mg LEFT JOIN FETCH mg.genre WHERE m.title LIKE %:keyword%")
    List<Movie> findByTitleContainingWithGenres(@Param("keyword") String keyword);
    List<Movie> findAllByTitle(String title);

    Page<Movie> findByMovieIdIn(List<Long> ids, Pageable pageable);
    
    // 장르별 영화 페이지네이션 조회 (N+1 문제 해결을 위한 JOIN FETCH)
    @Query("SELECT DISTINCT m FROM Movie m " +
           "JOIN FETCH m.movieGenres mg " +
           "JOIN FETCH mg.genre g " +
           "WHERE g.genreId IN :genreIds " +
           "AND m.posterUrl IS NOT NULL " +
           "ORDER BY m.totalView DESC, m.releaseDate DESC")
    Page<Movie> findByGenreIdsWithGenres(@Param("genreIds") List<Long> genreIds, Pageable pageable);
    
    // 모든 장르의 영화 조회 (장르 필터링 없음)
    @Query("SELECT DISTINCT m FROM Movie m " +
           "JOIN FETCH m.movieGenres mg " +
           "JOIN FETCH mg.genre g " +
           "WHERE m.posterUrl IS NOT NULL " +
           "ORDER BY m.totalView DESC, m.releaseDate DESC")
    Page<Movie> findAllWithGenresPageable(Pageable pageable);
    
    @Query("SELECT m FROM Movie m WHERE REPLACE(m.title, ' ', '') LIKE %:title%")
    List<Movie> findAllByTitleIgnoringSpace(@Param("title") String title);


    @Query("SELECT DISTINCT m FROM Movie m JOIN FETCH m.movieGenres mg JOIN FETCH mg.genre")
    List<Movie> findAllWithGenres();

    @Modifying
    @Query("UPDATE Movie m SET m.totalView = m.totalView + 1 WHERE m.movieId = :movieId")
    void incrementTotalView(@Param("movieId") Long movieId);

    @Query("SELECT m.movieId FROM Movie m")
    List<Long> findAllMovieIds();
}
