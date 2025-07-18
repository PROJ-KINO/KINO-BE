package com.hamss2.KINO.api.movieAdmin.repository;

import com.hamss2.KINO.api.entity.Actor;
import com.hamss2.KINO.api.entity.Movie;
import com.hamss2.KINO.api.entity.MovieActor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieActorRepository extends JpaRepository<MovieActor, Long> {
    boolean existsByMovieAndActor(Movie movie, Actor actor);

}
