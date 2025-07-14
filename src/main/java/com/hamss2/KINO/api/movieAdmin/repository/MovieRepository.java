package com.hamss2.KINO.api.movieAdmin.repository;

import com.hamss2.KINO.api.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieRepository extends JpaRepository<Movie, Long> {
}
