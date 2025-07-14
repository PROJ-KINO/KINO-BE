package com.hamss2.KINO.api.home.repository;

import com.hamss2.KINO.api.entity.Movie;
import com.hamss2.KINO.api.entity.MyPickMovie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MyPickMovieRepository extends JpaRepository<MyPickMovie, Long> {
    // 영화별 찜 개수 내림차순 Top 10
    @Query("""
    SELECT m.movie
    FROM MyPickMovie m
    GROUP BY m.movie
    ORDER BY COUNT(m) DESC
""")
    List<Movie> findTop10MoviesByPickCount();
}
