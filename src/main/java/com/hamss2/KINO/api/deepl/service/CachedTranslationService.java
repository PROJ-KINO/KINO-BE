package com.hamss2.KINO.api.deepl.service;

import com.hamss2.KINO.api.home.dto.res.HomeResponseDto;
import com.hamss2.KINO.api.home.dto.res.MovieDto;
import com.hamss2.KINO.api.home.dto.res.TeaserDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.hamss2.KINO.api.deepl.service.LibreTranslateService;

import java.util.ArrayList;
import java.util.List;
import com.hamss2.KINO.api.home.dto.res.ReviewDto;

@Service
@RequiredArgsConstructor
@Slf4j
public class CachedTranslationService {
    
    private final LibreTranslateService libreTranslateService;
    
    /**
     * 티저 데이터 번역 캐시
     */
    @Cacheable(value = "translatedTeaser", key = "#targetLang")
    public TeaserDto getTranslatedTeaser(TeaserDto originalTeaser, String targetLang) {
        if (originalTeaser == null || targetLang.equals("KO") || targetLang.isEmpty()) {
            return originalTeaser;
        }
        
        log.info("🎬 Translating teaser data for language: {}", targetLang);
        
        try {
            // 원본 복사본 생성
            TeaserDto translatedTeaser = new TeaserDto();
            translatedTeaser.setMovieId(originalTeaser.getMovieId());
            translatedTeaser.setTeaserUrl(originalTeaser.getTeaserUrl());
            translatedTeaser.setStillCutUrl(originalTeaser.getStillCutUrl());
            translatedTeaser.setReleaseDate(originalTeaser.getReleaseDate());
            translatedTeaser.setRunningTime(originalTeaser.getRunningTime());
            
            // 번역 대상 필드들
            if (originalTeaser.getTitle() != null && !originalTeaser.getTitle().trim().isEmpty()) {
                String translatedTitle = libreTranslateService.translate(originalTeaser.getTitle(), targetLang);
                translatedTeaser.setTitle(translatedTitle);
                log.info("✅ Teaser title translated: '{}' -> '{}'", originalTeaser.getTitle(), translatedTitle);
            } else {
                translatedTeaser.setTitle(originalTeaser.getTitle());
            }
            
            if (originalTeaser.getPlot() != null && !originalTeaser.getPlot().trim().isEmpty()) {
                String translatedPlot = libreTranslateService.translate(originalTeaser.getPlot(), targetLang);
                translatedTeaser.setPlot(translatedPlot);
                log.info("✅ Teaser plot translated (length: {} -> {})", originalTeaser.getPlot().length(), translatedPlot.length());
            } else {
                translatedTeaser.setPlot(originalTeaser.getPlot());
            }
            
            if (originalTeaser.getGenres() != null) {
                List<String> translatedGenres = originalTeaser.getGenres().stream()
                        .map(genre -> {
                            try {
                                if (genre != null && !genre.trim().isEmpty()) {
                                    String translatedGenre = libreTranslateService.translate(genre, targetLang);
                                    log.info("✅ Genre translated: '{}' -> '{}'", genre, translatedGenre);
                                    return translatedGenre;
                                }
                                return genre;
                            } catch (Exception e) {
                                log.warn("⚠️ Failed to translate genre '{}': {}", genre, e.getMessage());
                                return genre;
                            }
                        })
                        .toList();
                translatedTeaser.setGenres(translatedGenres);
            }
            
            log.info("✅ Teaser translation completed for language: {}", targetLang);
            return translatedTeaser;
            
        } catch (Exception e) {
            log.error("❌ Error translating teaser: {}", e.getMessage(), e);
            return originalTeaser;
        }
    }
    
    /**
     * 영어 문자가 포함되어 있는지 확인
     */
    private boolean containsEnglishChars(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }
        return text.matches(".*[a-zA-Z].*");
    }
    
    /**
     * 박스오피스 영화 목록 번역 캐시
     */
    @Cacheable(value = "translatedBoxOffice", key = "#targetLang")
    public List<MovieDto> getTranslatedBoxOfficeMovies(List<MovieDto> originalMovies, String targetLang) {
        try {
            return translateMovieList(originalMovies, targetLang, "BoxOffice");
        } catch (Exception e) {
            log.warn("⚠️ BoxOffice translation failed, returning original: {}", e.getMessage());
            return originalMovies;
        }
    }
    
    /**
     * 일별 TOP 영화 목록 번역 캐시
     */
    @Cacheable(value = "translatedDailyTop", key = "#targetLang")
    public List<MovieDto> getTranslatedDailyTopMovies(List<MovieDto> originalMovies, String targetLang) {
        try {
            return translateMovieList(originalMovies, targetLang, "DailyTop");
        } catch (Exception e) {
            log.warn("⚠️ DailyTop translation failed, returning original: {}", e.getMessage());
            return originalMovies;
        }
    }
    
    /**
     * 월별 TOP 영화 목록 번역 캐시
     */
    @Cacheable(value = "translatedMonthlyTop", key = "#targetLang")
    public List<MovieDto> getTranslatedMonthlyTopMovies(List<MovieDto> originalMovies, String targetLang) {
        try {
            return translateMovieList(originalMovies, targetLang, "MonthlyTop");
        } catch (Exception e) {
            log.warn("⚠️ MonthlyTop translation failed, returning original: {}", e.getMessage());
            return originalMovies;
        }
    }
    
    /**
     * 추천 영화 목록 번역 캐시
     */
    @Cacheable(value = "translatedRecommended", key = "#targetLang")
    public List<MovieDto> getTranslatedRecommendedMovies(List<MovieDto> originalMovies, String targetLang) {
        try {
            return translateMovieList(originalMovies, targetLang, "Recommended");
        } catch (Exception e) {
            log.warn("⚠️ Recommended translation failed, returning original: {}", e.getMessage());
            return originalMovies;
        }
    }

    /**
     * 사용자 좋아요 탑 10 리뷰 번역 캐시
     */
    @Cacheable(value = "translatedTopLikeReviews", key = "#targetLang")
    public List<ReviewDto> getTranslatedTopLikeReviews(List<ReviewDto> originalReviews, String targetLang) {
        try {
            return translateReviewList(originalReviews, targetLang, "TopLikeReviews");
        } catch (Exception e) {
            log.warn("⚠️ TopLikeReviews translation failed, returning original: {}", e.getMessage());
            return originalReviews;
        }
    }

    /**
     * 사용자 좋아요 탑 10 영화 번역 캐시
     */
    @Cacheable(value = "translatedTopPickMovies", key = "#targetLang")
    public List<MovieDto> getTranslatedTopPickMovies(List<MovieDto> originalMovies, String targetLang) {
        try {
            return translateMovieList(originalMovies, targetLang, "TopPickMovies");
        } catch (Exception e) {
            log.warn("⚠️ TopPickMovies translation failed, returning original: {}", e.getMessage());
            return originalMovies;
        }
    }
    
    /**
     * 영화 목록 번역 공통 메소드 (단순화된 개별 번역 방식)
     */
    private List<MovieDto> translateMovieList(List<MovieDto> originalMovies, String targetLang, String listType) {
        if (originalMovies == null || originalMovies.isEmpty() || targetLang.equals("KO") || targetLang.isEmpty()) {
            return originalMovies;
        }
        
        log.info("🎥 Translating {} movie list for language: {} (count: {})", listType, targetLang, originalMovies.size());
        
        try {
            // 배치 번역 방식으로 변경 (효율성 향상)
            List<MovieDto> translatedMovies = translateMovieListBatch(originalMovies, targetLang);
            
            log.info("✅ {} movie list translation completed for language: {}", listType, targetLang);
            return translatedMovies;
            
        } catch (Exception e) {
            log.error("❌ Error translating {} movie list: {}", listType, e.getMessage(), e);
            return originalMovies;
        }
    }
    
    /**
     * 영화 목록 배치 번역
     */
    private List<MovieDto> translateMovieListBatch(List<MovieDto> originalMovies, String targetLang) {
        try {
            // 모든 번역할 텍스트 수집
            List<String> textsToTranslate = new ArrayList<>();
            List<MovieDto> translatedMovies = new ArrayList<>();
            
            for (MovieDto movie : originalMovies) {
                MovieDto translatedMovie = new MovieDto();
                
                // 번역 불필요한 필드 복사
                translatedMovie.setMovieId(movie.getMovieId());
                translatedMovie.setReleaseDate(movie.getReleaseDate());
                translatedMovie.setRunningTime(movie.getRunningTime());
                translatedMovie.setStillCutUrl(movie.getStillCutUrl());
                translatedMovie.setPosterUrl(movie.getPosterUrl());
                
                // 번역할 텍스트 수집
                if (movie.getTitle() != null && !movie.getTitle().trim().isEmpty()) {
                    textsToTranslate.add(movie.getTitle());
                }
                if (movie.getPlot() != null && !movie.getPlot().trim().isEmpty()) {
                    textsToTranslate.add(movie.getPlot());
                }
                if (movie.getAgeRating() != null && !movie.getAgeRating().trim().isEmpty()) {
                    textsToTranslate.add(movie.getAgeRating());
                }
                if (movie.getGenres() != null) {
                    textsToTranslate.addAll(movie.getGenres().stream()
                            .filter(genre -> genre != null && !genre.trim().isEmpty())
                            .toList());
                }
                
                translatedMovies.add(translatedMovie);
            }
            
            // 배치 번역 실행
            if (!textsToTranslate.isEmpty()) {
                List<String> translatedTexts = libreTranslateService.translateBatch(textsToTranslate, targetLang);
                
                // 번역된 텍스트를 영화에 적용
                int textIndex = 0;
                
                for (MovieDto movie : originalMovies) {
                    MovieDto translatedMovie = translatedMovies.get(originalMovies.indexOf(movie));
                    
                    if (movie.getTitle() != null && !movie.getTitle().trim().isEmpty() && textIndex < translatedTexts.size()) {
                        translatedMovie.setTitle(translatedTexts.get(textIndex++));
                    } else {
                        translatedMovie.setTitle(movie.getTitle());
                    }
                    
                    if (movie.getPlot() != null && !movie.getPlot().trim().isEmpty() && textIndex < translatedTexts.size()) {
                        translatedMovie.setPlot(translatedTexts.get(textIndex++));
                    } else {
                        translatedMovie.setPlot(movie.getPlot());
                    }
                    
                    if (movie.getAgeRating() != null && !movie.getAgeRating().trim().isEmpty() && textIndex < translatedTexts.size()) {
                        translatedMovie.setAgeRating(translatedTexts.get(textIndex++));
                    } else {
                        translatedMovie.setAgeRating(movie.getAgeRating());
                    }
                    
                    if (movie.getGenres() != null) {
                        List<String> translatedGenres = new ArrayList<>();
                        for (String genre : movie.getGenres()) {
                            if (genre != null && !genre.trim().isEmpty() && textIndex < translatedTexts.size()) {
                                translatedGenres.add(translatedTexts.get(textIndex++));
                            } else {
                                translatedGenres.add(genre);
                            }
                        }
                        translatedMovie.setGenres(translatedGenres);
                    }
                }
                
                log.info("✅ Movie list batch translation completed with {} translations", translatedTexts.size());
                return translatedMovies;
            }
            
            return originalMovies;
            
        } catch (Exception e) {
            log.error("❌ Error in batch translation: {}", e.getMessage());
            return originalMovies;
        }
    }

    /**
     * 리뷰 목록 번역
     */
    private List<ReviewDto> translateReviewList(List<ReviewDto> originalReviews, String targetLang, String listType) {
        log.info("🔄 Translating {} reviews for language: {}", listType, targetLang);
        
        List<ReviewDto> translatedReviews = new ArrayList<>();
        
        for (ReviewDto review : originalReviews) {
            ReviewDto translatedReview = new ReviewDto();
            
            // 기본 필드 복사
            translatedReview.setReviewId(review.getReviewId());
            translatedReview.setMovieId(review.getMovieId());
            translatedReview.setReleaseDate(review.getReleaseDate());
            translatedReview.setRunningTime(review.getRunningTime());
            translatedReview.setStillCutUrl(review.getStillCutUrl());
            translatedReview.setPosterUrl(review.getPosterUrl());
            
            // 번역이 필요한 텍스트 필드들
            if (review.getReviewTitle() != null && !review.getReviewTitle().trim().isEmpty()) {
                String translatedTitle = libreTranslateService.translate(review.getReviewTitle(), targetLang);
                translatedReview.setReviewTitle(translatedTitle);
            }
            
            if (review.getContent() != null && !review.getContent().trim().isEmpty()) {
                String translatedContent = libreTranslateService.translate(review.getContent(), targetLang);
                translatedReview.setContent(translatedContent);
            }
            
            if (review.getMovieTitle() != null && !review.getMovieTitle().trim().isEmpty()) {
                String translatedMovieTitle = libreTranslateService.translate(review.getMovieTitle(), targetLang);
                translatedReview.setMovieTitle(translatedMovieTitle);
            }
            
            if (review.getPlot() != null && !review.getPlot().trim().isEmpty()) {
                String translatedPlot = libreTranslateService.translate(review.getPlot(), targetLang);
                translatedReview.setPlot(translatedPlot);
            }
            
            if (review.getAgeRating() != null && !review.getAgeRating().trim().isEmpty()) {
                String translatedAgeRating = libreTranslateService.translate(review.getAgeRating(), targetLang);
                translatedReview.setAgeRating(translatedAgeRating);
            }
            
            // 장르 번역
            if (review.getGenres() != null && !review.getGenres().isEmpty()) {
                List<String> translatedGenres = new ArrayList<>();
                for (String genre : review.getGenres()) {
                    if (genre != null && !genre.trim().isEmpty()) {
                        String translatedGenre = libreTranslateService.translate(genre, targetLang);
                        translatedGenres.add(translatedGenre);
                    }
                }
                translatedReview.setGenres(translatedGenres);
            }
            
            translatedReviews.add(translatedReview);
        }
        
        log.info("✅ Successfully translated {} reviews for language: {}", listType, targetLang);
        return translatedReviews;
    }
    
    /**
     * 번역된 티저를 캐시에 저장
     */
    @CachePut(value = "translatedTeaser", key = "#targetLang")
    public TeaserDto putTranslatedTeaser(TeaserDto translatedTeaser, String targetLang) {
        try {
            log.info("💾 Storing translated teaser in cache for language: {}", targetLang);
            return translatedTeaser;
        } catch (Exception e) {
            log.warn("⚠️ Failed to cache translated teaser: {}", e.getMessage());
            return translatedTeaser; // 캐시 실패해도 번역된 데이터는 반환
        }
    }
    
    /**
     * 번역된 박스오피스 목록을 캐시에 저장
     */
    @CachePut(value = "translatedBoxOffice", key = "#targetLang")
    public List<MovieDto> putTranslatedBoxOfficeMovies(List<MovieDto> translatedMovies, String targetLang) {
        try {
            log.info("💾 Storing translated BoxOffice movies in cache for language: {} ({} movies)", targetLang, translatedMovies.size());
            return translatedMovies;
        } catch (Exception e) {
            log.warn("⚠️ Failed to cache translated BoxOffice movies: {}", e.getMessage());
            return translatedMovies; // 캐시 실패해도 번역된 데이터는 반환
        }
    }
    
    /**
     * 번역된 일별 TOP 목록을 캐시에 저장
     */
    @CachePut(value = "translatedDailyTop", key = "#targetLang")
    public List<MovieDto> putTranslatedDailyTopMovies(List<MovieDto> translatedMovies, String targetLang) {
        try {
            log.info("💾 Storing translated DailyTop movies in cache for language: {} ({} movies)", targetLang, translatedMovies.size());
            return translatedMovies;
        } catch (Exception e) {
            log.warn("⚠️ Failed to cache translated DailyTop movies: {}", e.getMessage());
            return translatedMovies; // 캐시 실패해도 번역된 데이터는 반환
        }
    }
    
    /**
     * 번역된 월별 TOP 목록을 캐시에 저장
     */
    @CachePut(value = "translatedMonthlyTop", key = "#targetLang")
    public List<MovieDto> putTranslatedMonthlyTopMovies(List<MovieDto> translatedMovies, String targetLang) {
        try {
            log.info("💾 Storing translated MonthlyTop movies in cache for language: {} ({} movies)", targetLang, translatedMovies.size());
            return translatedMovies;
        } catch (Exception e) {
            log.warn("⚠️ Failed to cache translated MonthlyTop movies: {}", e.getMessage());
            return translatedMovies; // 캐시 실패해도 번역된 데이터는 반환
        }
    }
    
    /**
     * 번역된 추천 목록을 캐시에 저장
     */
    @CachePut(value = "translatedRecommended", key = "#targetLang")
    public List<MovieDto> putTranslatedRecommendedMovies(List<MovieDto> translatedMovies, String targetLang) {
        try {
            log.info("💾 Storing translated Recommended movies in cache for language: {} ({} movies)", targetLang, translatedMovies.size());
            return translatedMovies;
        } catch (Exception e) {
            log.warn("⚠️ Failed to cache translated Recommended movies: {}", e.getMessage());
            return translatedMovies; // 캐시 실패해도 번역된 데이터는 반환
        }
    }

    /**
     * 번역된 좋아요 탑 10 리뷰를 캐시에 저장
     */
    @CachePut(value = "translatedTopLikeReviews", key = "#targetLang")
    public List<ReviewDto> putTranslatedTopLikeReviews(List<ReviewDto> translatedReviews, String targetLang) {
        try {
            log.info("💾 Storing translated TopLikeReviews in cache for language: {} ({} reviews)", targetLang, translatedReviews.size());
            return translatedReviews;
        } catch (Exception e) {
            log.warn("⚠️ Failed to cache translated TopLikeReviews: {}", e.getMessage());
            return translatedReviews; // 캐시 실패해도 번역된 데이터는 반환
        }
    }

    /**
     * 번역된 좋아요 탑 10 영화를 캐시에 저장
     */
    @CachePut(value = "translatedTopPickMovies", key = "#targetLang")
    public List<MovieDto> putTranslatedTopPickMovies(List<MovieDto> translatedMovies, String targetLang) {
        try {
            log.info("💾 Storing translated TopPickMovies in cache for language: {} ({} movies)", targetLang, translatedMovies.size());
            return translatedMovies;
        } catch (Exception e) {
            log.warn("⚠️ Failed to cache translated TopPickMovies: {}", e.getMessage());
            return translatedMovies; // 캐시 실패해도 번역된 데이터는 반환
        }
    }
    
    /**
     * 모든 번역 캐시 삭제 (갱신용)
     */
    @CacheEvict(value = {"translatedTeaser", "translatedBoxOffice", "translatedDailyTop", "translatedMonthlyTop", "translatedRecommended"}, allEntries = true)
    public void evictAllTranslationCaches() {
        log.info("🗑️ All translation caches evicted");
    }
} 