package com.hamss2.KINO.api.movieAdmin.scheduler;

import com.hamss2.KINO.api.movieAdmin.service.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TmdbSyncScheduler {

    private final MovieService movieService;

    // 영화 동기화 (1~1000페이지)
    @Scheduled(cron = "0 0 3 * * *") // 매일 새벽 3시
    public void syncMovies() {
        movieService.fetchAndSaveMovies(1, 1000);
    }

    // 장르 동기화
    @Scheduled(cron = "0 5 3 * * *") // 매일 새벽 3시 5분
    public void syncGenres() {
        movieService.fetchAndSaveGenres();
    }


}
