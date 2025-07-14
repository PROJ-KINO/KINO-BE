package com.hamss2.KINO.api.movieAdmin.controller;

import com.hamss2.KINO.api.movieAdmin.service.MovieService;
import com.hamss2.KINO.common.reponse.ApiResponse;
import com.hamss2.KINO.common.reponse.SuccessStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tmdb")
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;

    @PostMapping("/movie")
    public ApiResponse<Void> fetchAndSaveMovies(
            @RequestParam(defaultValue = "1") int start,
            @RequestParam(defaultValue = "1000") int end) {
        movieService.fetchAndSaveMovies(start, end);
        return ApiResponse.success_only(SuccessStatus.SEND_HEALTH_SUCCESS).getBody();
    }

    @PostMapping("/genre")
    public ApiResponse<Void>  syncGenres() {
        movieService.fetchAndSaveGenres();
        return ApiResponse.success_only(SuccessStatus.SEND_HEALTH_SUCCESS).getBody();
    }

}
