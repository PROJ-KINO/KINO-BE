package com.hamss2.KINO.api.movieAdmin.repository;

import com.hamss2.KINO.api.entity.MovieGenre;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovieGenreRepository extends JpaRepository<MovieGenre, Long> {
    List<MovieGenre> findByMovie_MovieId(Long movieId);
}
