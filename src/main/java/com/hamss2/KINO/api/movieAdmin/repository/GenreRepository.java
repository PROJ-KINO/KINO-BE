package com.hamss2.KINO.api.movieAdmin.repository;

import com.hamss2.KINO.api.entity.Genre;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GenreRepository extends JpaRepository<Genre, Long> {
}
