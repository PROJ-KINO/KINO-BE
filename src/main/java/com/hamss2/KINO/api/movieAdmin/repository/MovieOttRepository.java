package com.hamss2.KINO.api.movieAdmin.repository;

import com.hamss2.KINO.api.entity.Movie;
import com.hamss2.KINO.api.entity.MovieOtt;
import com.hamss2.KINO.api.entity.Ott;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieOttRepository extends JpaRepository<MovieOtt, Long> {
    // 영화+OTT 조합 중복 저장 방지용 메서드
    boolean existsByMovieAndOtt(Movie movie, Ott ott);
}
