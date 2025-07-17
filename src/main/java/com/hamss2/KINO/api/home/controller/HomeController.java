package com.hamss2.KINO.api.home.controller;

import com.hamss2.KINO.api.home.dto.req.GenreSelectReq;
import com.hamss2.KINO.api.home.dto.res.HomeResponseDto;
import com.hamss2.KINO.api.home.dto.res.MovieDto;
import com.hamss2.KINO.api.home.service.HomeService;
import com.hamss2.KINO.common.reponse.ApiResponse;
import com.hamss2.KINO.common.reponse.SuccessStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class HomeController {

    private final HomeService homeService;

    @GetMapping("/home")
    public ResponseEntity<ApiResponse<HomeResponseDto>> getHomeData(@AuthenticationPrincipal String userId) {
        HomeResponseDto homeResponseDto = homeService.getHomeData(Long.valueOf(userId));
        return ApiResponse.success(SuccessStatus.SEND_HOME_SUCCESS, homeResponseDto);
    }

    @PostMapping("/user/genre")
    public ResponseEntity<ApiResponse<Void>> selectGenre(@RequestBody GenreSelectReq req, @AuthenticationPrincipal String userId) {
        homeService.saveUserGenres(req, Long.valueOf(userId));
        return ApiResponse.success_only(SuccessStatus.SEND_USER_GENRE_SELECT_SUCCESS);
    }

    @GetMapping("/movies/search")
    public ResponseEntity<ApiResponse<List<MovieDto>>> searchMovies(@RequestParam String keyword) {
        List<MovieDto> result = homeService.searchMovies(keyword);
        return ApiResponse.success(SuccessStatus.SEARCH_MOVIE_SUCCESS, result);
    }
}
