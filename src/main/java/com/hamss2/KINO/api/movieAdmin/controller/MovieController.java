package com.hamss2.KINO.api.movieAdmin.controller;

import com.hamss2.KINO.api.movieAdmin.service.MovieService;
import com.hamss2.KINO.common.reponse.ApiResponse;
import com.hamss2.KINO.common.reponse.SuccessStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;

    @PostMapping("/tmdb-movie")
    public ResponseEntity<ApiResponse<Void>> fetchAndSaveMovies() {
        movieService.fetchAndSaveMovies(137, 500, "popularity.desc");
        movieService.fetchAndSaveMovies(1, 20, "release_date.desc");
        return ApiResponse.success_only(SuccessStatus.SYNC_TMDB_MOVIES_SUCCESS);
    }

    @PostMapping("/tmdb-genre")
    public ResponseEntity<ApiResponse<Void>> syncGenres() {
        movieService.fetchAndSaveGenres();
        return ApiResponse.success_only(SuccessStatus.SYNC_TMDB_GENRES_SUCCESS);
    }

}
