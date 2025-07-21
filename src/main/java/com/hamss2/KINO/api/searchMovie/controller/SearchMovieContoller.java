package com.hamss2.KINO.api.searchMovie.controller;

import com.hamss2.KINO.api.deepl.annotation.Translate;
import com.hamss2.KINO.api.movieAdmin.service.MovieService;
import org.springframework.data.domain.Page;
import com.hamss2.KINO.api.searchMovie.dto.MovieResDto;
import com.hamss2.KINO.common.reponse.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    /**
     * 🎬 영화 목록 페이지네이션 조회
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지 크기 (기본값: 12)
     * @param genreIds 장르 ID 목록 (선택적, 없으면 전체 조회)
     * @return 페이지네이션된 영화 목록
     */
    @GetMapping("/all")
    @Translate
    public ResponseEntity<ApiResponse<Page<MovieResDto>>> searchAllMovies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) List<Long> genreIds
            ) {
        Page<MovieResDto> dto = movieService.allMovies(PageRequest.of(page, size), genreIds);

        return ApiResponse.success(SEARCH_ALL_MOVIE_SUCCESS, dto);
    }
}
