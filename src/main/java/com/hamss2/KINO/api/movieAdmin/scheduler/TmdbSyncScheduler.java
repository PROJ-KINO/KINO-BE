package com.hamss2.KINO.api.movieAdmin.scheduler;

import com.hamss2.KINO.api.movieAdmin.service.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TmdbSyncScheduler {

    private final MovieService movieService;

    // 영화 동기화
    @Scheduled(cron = "0 0 3 * * *") // 매일 새벽 3시 인기순 1~ 40page(800편)
    public void syncMovies() {
        movieService.fetchAndSaveMovies(1, 40, "popularity.desc");
    }
    @Scheduled(cron = "0 10 3 * * *") // 매일 3:10 최신 개봉일순 1~15page (300편)
    public void syncRecentMovies() {
        movieService.fetchAndSaveMovies(1, 15, "release_date.desc");
    }

    // 장르 동기화
    @Scheduled(cron = "0 20 3 * * *") // 매일 새벽 3시 5분
    public void syncGenres() {
        movieService.fetchAndSaveGenres();
    }


}
