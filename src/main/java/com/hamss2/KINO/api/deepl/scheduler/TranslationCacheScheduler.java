package com.hamss2.KINO.api.deepl.scheduler;

import com.hamss2.KINO.api.deepl.service.CachedTranslationService;
import com.hamss2.KINO.api.home.service.HomeService;
import com.hamss2.KINO.api.home.dto.res.HomeResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TranslationCacheScheduler {
    
    private final CachedTranslationService cachedTranslationService;
    private final HomeService homeService;
    
    /**
     * 30분마다 번역 캐시 갱신
     */
    @Scheduled(fixedRate = 30 * 60 * 1000) // 30분 = 30 * 60 * 1000 밀리초
    public void refreshTranslationCaches() {
        log.info("🔄 Starting translation cache refresh (30min interval)...");
        
        try {
            // 2. 최신 홈 데이터 조회 (임시 사용자 ID로 조회)
            // 실제 사용자 ID가 필요하다면 이 부분을 수정해야 합니다
            HomeResponseDto homeData = getLatestHomeData();
            
            if (homeData != null) {
                // 3. 영어 번역 데이터 미리 생성하여 캐시에 저장
                refreshCacheForLanguage(homeData, "EN");
                
                // 필요시 다른 언어도 추가 가능
                // refreshCacheForLanguage(homeData, "JA");
                // refreshCacheForLanguage(homeData, "ZH");
            }
            
            log.info("✅ Translation cache refresh completed successfully");
            
        } catch (Exception e) {
            log.error("❌ Translation cache refresh failed: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 특정 언어에 대한 캐시 갱신
     */
    private void refreshCacheForLanguage(HomeResponseDto homeData, String targetLang) {
        log.info("🌐 Refreshing cache for language: {}", targetLang);
        
        try {
            // 티저 번역 캐시 생성
            if (homeData.getTeaser() != null) {
                cachedTranslationService.getTranslatedTeaser(homeData.getTeaser(), targetLang);
                log.info("✅ Teaser cache refreshed for {}", targetLang);
            }
            
            // 박스오피스 번역 캐시 생성
            if (homeData.getBoxOfficeMovieList() != null && !homeData.getBoxOfficeMovieList().isEmpty()) {
                cachedTranslationService.getTranslatedBoxOfficeMovies(homeData.getBoxOfficeMovieList(), targetLang);
                log.info("✅ BoxOffice cache refreshed for {} ({} movies)", targetLang, homeData.getBoxOfficeMovieList().size());
            }
            
            // 일별 TOP 번역 캐시 생성
            if (homeData.getDailyTopMovieList() != null && !homeData.getDailyTopMovieList().isEmpty()) {
                cachedTranslationService.getTranslatedDailyTopMovies(homeData.getDailyTopMovieList(), targetLang);
                log.info("✅ DailyTop cache refreshed for {} ({} movies)", targetLang, homeData.getDailyTopMovieList().size());
            }
            
            // 월별 TOP 번역 캐시 생성
            if (homeData.getMonthlyTopMovieList() != null && !homeData.getMonthlyTopMovieList().isEmpty()) {
                cachedTranslationService.getTranslatedMonthlyTopMovies(homeData.getMonthlyTopMovieList(), targetLang);
                log.info("✅ MonthlyTop cache refreshed for {} ({} movies)", targetLang, homeData.getMonthlyTopMovieList().size());
            }
            
            // 추천 번역 캐시 생성
            if (homeData.getRecommendedMovieList() != null && !homeData.getRecommendedMovieList().isEmpty()) {
                cachedTranslationService.getTranslatedRecommendedMovies(homeData.getRecommendedMovieList(), targetLang);
                log.info("✅ Recommended cache refreshed for {} ({} movies)", targetLang, homeData.getRecommendedMovieList().size());
            }
            
            // 사용자 좋아요 탑 10 리뷰 번역 캐시 생성
            if (homeData.getTopLikeReviewList() != null && !homeData.getTopLikeReviewList().isEmpty()) {
                cachedTranslationService.getTranslatedTopLikeReviews(homeData.getTopLikeReviewList(), targetLang);
                log.info("✅ TopLikeReviews cache refreshed for {} ({} reviews)", targetLang, homeData.getTopLikeReviewList().size());
            }
            
            // 사용자 좋아요 탑 10 영화 번역 캐시 생성
            if (homeData.getTopPickMovieList() != null && !homeData.getTopPickMovieList().isEmpty()) {
                cachedTranslationService.getTranslatedTopPickMovies(homeData.getTopPickMovieList(), targetLang);
                log.info("✅ TopPickMovies cache refreshed for {} ({} movies)", targetLang, homeData.getTopPickMovieList().size());
            }
            
        } catch (Exception e) {
            log.error("❌ Failed to refresh cache for language {}: {}", targetLang, e.getMessage());
        }
    }
    
    /**
     * 최신 홈 데이터 조회 (시스템 사용자로 조회)
     */
    private HomeResponseDto getLatestHomeData() {
        try {
            // 시스템 사용자 ID (1L)로 홈 데이터 조회
            // 캐시 갱신용이므로 특정 사용자 데이터가 아닌 일반적인 데이터 조회
            Long systemUserId = 1L;
            
            log.info("📊 Fetching latest home data for cache refresh (userId: {})", systemUserId);
            HomeResponseDto homeData = homeService.getHomeData(systemUserId);
            
            if (homeData != null) {
                log.info("✅ Successfully fetched home data for cache refresh");
                return homeData;
            } else {
                log.warn("⚠️ Home data is null");
                return null;
            }
            
        } catch (Exception e) {
            log.error("❌ Failed to get latest home data: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 수동으로 캐시 갱신 트리거 (테스트용)
     */
    public void manualRefreshCache() {
        log.info("🔧 Manual cache refresh triggered");
        
        try {
            // 기존 캐시를 삭제하지 않고 새로운 설정으로 점진적으로 갱신
            log.info("🔄 Refreshing translation caches with new configuration (keeping existing caches)...");
            refreshTranslationCaches();
            
            log.info("✅ Manual cache refresh completed successfully");
            
        } catch (Exception e) {
            log.error("❌ Manual cache refresh failed: {}", e.getMessage(), e);
            throw new RuntimeException("캐시 갱신에 실패했습니다: " + e.getMessage(), e);
        }
    }
} 