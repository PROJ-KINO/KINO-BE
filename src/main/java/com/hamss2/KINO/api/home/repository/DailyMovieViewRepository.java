package com.hamss2.KINO.api.home.repository;

import com.hamss2.KINO.api.entity.DailyMovieView;
import com.hamss2.KINO.api.entity.Movie;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyMovieViewRepository extends JpaRepository<DailyMovieView, Long> {
    // 오늘 기준 TOP 10 조회수
    @Query("""
     SELECT d FROM DailyMovieView d JOIN FETCH d.movie
    WHERE d.viewDate = :date
    ORDER BY d.dailyView DESC
    """)
    List<DailyMovieView> findByViewDateOrderByDailyViewDesc(@Param("date") LocalDate viewDate, Pageable pageable);

    @Query("""
    SELECT d.movie FROM DailyMovieView d
    WHERE d.viewDate BETWEEN :start AND :end GROUP BY d.movie
    ORDER BY SUM(d.dailyView) DESC
    """)
    List<Movie> findTop10MovieByMonthView(@Param("start") LocalDate start, @Param("end") LocalDate end, Pageable pageable);

    Optional<DailyMovieView> findByMovieAndViewDate(Movie movie, LocalDate viewDate);
}
