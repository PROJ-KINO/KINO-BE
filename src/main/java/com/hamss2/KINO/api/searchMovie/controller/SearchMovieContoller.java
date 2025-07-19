package com.hamss2.KINO.api.searchMovie.controller;

import com.hamss2.KINO.api.movieAdmin.service.MovieService;
import org.springframework.data.domain.Page;
import com.hamss2.KINO.api.searchMovie.dto.MovieResDto;
import com.hamss2.KINO.common.reponse.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.hamss2.KINO.common.reponse.SuccessStatus.SEARCH_ALL_MOVIE_SUCCESS;

@RestController
@RequestMapping("/api/movie")
@RequiredArgsConstructor
@Slf4j
public class SearchMovieContoller {
    private final MovieService movieService;

    @GetMapping("/r")
    public ResponseEntity<ApiResponse<List<MovieResDto>>> searchAllMovie() {
        List<MovieResDto> dto = movieService.allMovie();

        return ApiResponse.success(SEARCH_ALL_MOVIE_SUCCESS, dto);
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<Page<MovieResDto>>> searchAllMovies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam List<Long> ids
    ) {
        Page<MovieResDto> dto = movieService.allMovies(PageRequest.of(page, size), ids);

        return ApiResponse.success(SEARCH_ALL_MOVIE_SUCCESS, dto);
    }
}
