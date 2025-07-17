package com.hamss2.KINO.api.searchMovie.controller;

import com.hamss2.KINO.api.movieAdmin.service.MovieService;
import com.hamss2.KINO.api.searchMovie.dto.MovieResDto;
import com.hamss2.KINO.common.reponse.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.hamss2.KINO.common.reponse.SuccessStatus.SEARCH_ALL_MOVIE_SUCCESS;

@RestController
@RequestMapping("/api/movie")
@RequiredArgsConstructor
@Slf4j
public class SearchMovieContoller {
    private final MovieService movieService;

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<MovieResDto>>> searchAllMovies() {
        List<MovieResDto> dto = movieService.allMovie();

        return ApiResponse.success(SEARCH_ALL_MOVIE_SUCCESS, dto);
    }
}
