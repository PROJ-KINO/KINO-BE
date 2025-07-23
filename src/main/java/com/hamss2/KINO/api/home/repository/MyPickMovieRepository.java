package com.hamss2.KINO.api.home.repository;

import com.hamss2.KINO.api.entity.Movie;
import com.hamss2.KINO.api.entity.MyPickMovie;
import com.hamss2.KINO.api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MyPickMovieRepository extends JpaRepository<MyPickMovie, Long> {
    // 영화별 찜 개수 내림차순 Top 10
    @Query("""
    SELECT m.movie
    FROM MyPickMovie m
    GROUP BY m.movie
    ORDER BY COUNT(m) DESC
""")
    List<Movie> findTop10MoviesByPickCount();
    // 유저와 영화로 찜 확인
    Optional<MyPickMovie> findByUserAndMovie(User user, Movie movie);
    // 찜 해제
    void deleteByUserAndMovie(User user, Movie movie);
    
    // 중복 데이터 확인
    @Query("SELECT COUNT(m) FROM MyPickMovie m WHERE m.user = :user AND m.movie = :movie")
    long countByUserAndMovie(User user, Movie movie);
    
    // 중복 데이터 삭제 (가장 오래된 것만 남기고 나머지 삭제)
    @Query("DELETE FROM MyPickMovie m WHERE m.id NOT IN (SELECT MIN(m2.id) FROM MyPickMovie m2 WHERE m2.user = :user AND m2.movie = :movie GROUP BY m2.user, m2.movie) AND m.user = :user AND m.movie = :movie")
    void deleteDuplicateByUserAndMovie(User user, Movie movie);
}
