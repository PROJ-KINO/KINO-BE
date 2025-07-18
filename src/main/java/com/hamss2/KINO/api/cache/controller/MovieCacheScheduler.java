package com.hamss2.KINO.api.cache.controller;

import com.hamss2.KINO.api.cache.service.CacheRefreshService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class MovieCacheScheduler {

    private final CacheRefreshService cacheRefreshService;

    @Scheduled(cron = "0 0 4 * * ?") // 매일 새벽 04시
    public void refreshMovieCache() {
        log.info("📌 영화 캐시 갱신 시작");

        cacheRefreshService.refreshMovieCache();
    }
}

