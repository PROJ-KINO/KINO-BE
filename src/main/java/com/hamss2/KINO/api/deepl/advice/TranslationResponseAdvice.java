package com.hamss2.KINO.api.deepl.advice;

import com.hamss2.KINO.api.deepl.annotation.Translate;
import com.hamss2.KINO.api.deepl.service.LibreTranslateService;
import com.hamss2.KINO.api.deepl.service.CachedTranslationService;
import com.hamss2.KINO.common.reponse.ApiResponse;
import com.hamss2.KINO.common.reponse.SuccessStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;
import com.hamss2.KINO.api.home.dto.res.*;
import java.util.concurrent.atomic.AtomicInteger;
import com.hamss2.KINO.api.movieDetail.dto.res.MovieDetailDto;

@ControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class TranslationResponseAdvice implements ResponseBodyAdvice<Object> {

    private final LibreTranslateService libreTranslateService;
    private final CachedTranslationService cachedTranslationService;

    @Override
    public boolean supports(MethodParameter returnType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        log.info("supports() called: converterType={}", converterType);

        return returnType.getContainingClass().isAnnotationPresent(Translate.class)
                || returnType.hasMethodAnnotation(Translate.class);
    }

    @Override
    public Object beforeBodyWrite(Object body,
                                  MethodParameter returnType,
                                  MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request,
                                  ServerHttpResponse response) {
        if (body == null) return body;
        
        // X-Target-Lang 헤더 추출
        List<String> langHeaders = request.getHeaders().get("X-Target-Lang");
        String targetLang = "";
        
        if (langHeaders != null && !langHeaders.isEmpty()) {
            targetLang = langHeaders.get(0).trim().toUpperCase();
        }
        
        log.info("🌍 X-Target-Lang: '{}' (empty: {})", targetLang, targetLang.isEmpty());
        
        // KO이거나 빈 값이면 번역하지 않음
        if (targetLang.isEmpty() || targetLang.equals("KO")) {
            log.info("✅ No translation needed (KO or empty)");
            return body;
        }
        
        // 영화 상세(MovieDetailDto) 응답이면 캐시 우선 처리
        if (body instanceof com.hamss2.KINO.common.reponse.ApiResponse apiResponse &&
            apiResponse.getData() instanceof MovieDetailDto movieDetailDto) {
            Long movieId = movieDetailDto.getMovieId();
            log.info("🎬 영화 상세 캐시 우선 처리: movieId={}, lang={}", movieId, targetLang);
            MovieDetailDto cached = cachedTranslationService.getMovieDetailFromCache(targetLang, movieId);
            if (cached != null) {
                log.info("💾 캐시 HIT: movieId={}, lang={}", movieId, targetLang);
                return ApiResponse.success(SuccessStatus.SEARCH_MOVIE_DETAIL_SUCCESS, cached).getBody();
            } else {
                log.info("⚡ 캐시 MISS: movieId={}, lang={}", movieId, targetLang);
                cachedTranslationService.translateAndCacheMovieDetail(movieDetailDto, targetLang, movieId);
                MovieDetailDto cachedAfterPut = cachedTranslationService.getMovieDetailFromCache(targetLang, movieId);
                log.info("[실시간번역] 캐시 저장 후 조회 결과: {}", cachedAfterPut);
                return ApiResponse.success(SuccessStatus.SEARCH_MOVIE_DETAIL_SUCCESS, cachedAfterPut).getBody();
            }
        }
        
        // EN이면 번역 수행
        if (targetLang.equals("EN")) {
            log.info("🔤 Starting English translation...");
            
            // 홈 API는 배치 번역 사용
            String path = request.getURI().getPath();
            if (path.contains("/api/home")) {
                log.info("🏠 Using batch translation for home API...");
                return translateHomeBatch(body, targetLang);
            }
            
            // 다른 API는 기존 방식 사용
            Set<Object> visited = new HashSet<>();
            translateFields(body, targetLang, visited);
            return body;
        }
        
        // 지원하지 않는 언어면 번역하지 않음
        log.info("⚠️ Unsupported language: '{}', returning original", targetLang);
        return body;
    }

    private void translateFields(Object obj, String targetLang, Set<Object> visited) {
        if (obj == null) return;
        
        // 순환 참조 방지: 이미 방문한 객체는 스킵
        if (visited.contains(obj)) {
            log.info("🔄 Circular reference detected, skipping: {}", obj.getClass().getName());
            return;
        }
        visited.add(obj);

        log.info("🔍 Analyzing object: {} (class: {})", 
                obj.toString().length() > 100 ? obj.toString().substring(0, 100) + "..." : obj.toString(), 
                obj.getClass().getName());

        if (obj instanceof List<?>) {
            log.info("📝 Found List with {} items", ((List<?>) obj).size());
            for (Object item : (List<?>) obj) translateFields(item, targetLang, visited);
            return;
        }
        if (obj instanceof java.util.Map<?,?>) {
            log.info("📝 Found Map with {} entries", ((java.util.Map<?,?>) obj).size());
            for (Object v : ((java.util.Map<?,?>) obj).values()) translateFields(v, targetLang, visited);
            return;
        }
        
        Class<?> cls = obj.getClass();
        if (isPrimitiveOrWrapper(cls) || cls.isEnum() || cls.getName().startsWith("java.lang") 
            || (cls.getName().startsWith("org.springframework") && !cls.getName().startsWith("org.springframework.data"))) {
            log.info("⚠️ Skipping class: {} (primitive/wrapper/enum/java/spring class)", cls.getName());
            return;
        }

        // 현재 클래스와 부모 클래스의 모든 필드 수집
        List<Field> allFields = new ArrayList<>();
        Class<?> currentClass = cls;
        while (currentClass != null && !currentClass.getName().startsWith("java.")) {
            allFields.addAll(Arrays.asList(currentClass.getDeclaredFields()));
            currentClass = currentClass.getSuperclass();
        }
        
        log.info("🔧 Processing {} fields in class: {} (including parent classes)", allFields.size(), cls.getName());
        for (Field f : allFields) {
            f.setAccessible(true);
            try {
                Object val = f.get(obj);
                if (val == null) continue;
                
                String fieldInfo = val.toString().length() > 50 ? val.toString().substring(0, 50) + "..." : val.toString();
                log.info("🔎 Field '{}' ({}): {}", f.getName(), f.getType().getSimpleName(), fieldInfo);
                
                if (val instanceof String s && !s.isBlank()) {
                    String translated = libreTranslateService.translate(s, targetLang);
                    log.info("✅ Translating '{}' to '{}' -> '{}'", s, targetLang, translated);
                    f.set(obj, translated);
                } else if (val != null) {
                    log.info("🔄 Recursing into field: {}", f.getName());
                    translateFields(val, targetLang, visited);
                }
            } catch (IllegalAccessException e) {
                log.warn("❌ Cannot access field: {}", f.getName());
            } catch (Exception e) {
                log.warn("⚠️ Error processing field {}: {}", f.getName(), e.getMessage());
            }
        }
        
        // 처리 완료 후 방문 기록에서 제거 (다른 경로로 다시 방문 가능하도록)
        visited.remove(obj);
    }

    private boolean isPrimitiveOrWrapper(Class<?> cls) {
        return cls.isPrimitive()
                || cls == Boolean.class
                || cls == Byte.class
                || cls == Character.class
                || cls == Short.class
                || cls == Integer.class
                || cls == Long.class
                || cls == Float.class
                || cls == Double.class
                || cls == Void.class;
    }

    /**
     * 홈 API 전용 하이브리드 번역 메소드 (캐시 + 실시간)
     */
    private Object translateHomeBatch(Object body, String targetLang) {
        try {
            if (!(body instanceof com.hamss2.KINO.common.reponse.ApiResponse)) {
                return body;
            }
            
            var apiResponse = (com.hamss2.KINO.common.reponse.ApiResponse<?>) body;
            if (!(apiResponse.getData() instanceof HomeResponseDto)) {
                return body;
            }
            
            HomeResponseDto homeData = (HomeResponseDto) apiResponse.getData();
            log.info("🏠 Starting hybrid translation for home data (cache + real-time)...");
            
            // 1. 캐시된 데이터 사용 (teaser, boxOffice, dailyTop, monthlyTop, recommended)
            replaceWithCachedTranslations(homeData, targetLang);
            
            // 2. 개인화 데이터는 실시간 번역 (topLikeReviewList, topPickMovieList)
            translatePersonalizedData(homeData, targetLang);
            
            log.info("✅ Hybrid translation completed for home data");
            return body;
            
        } catch (Exception e) {
            log.error("❌ Hybrid translation failed, falling back to original: {}", e.getMessage());
            return body; // 실패시 원본 반환
        }
    }
    
    /**
     * 캐시된 번역 데이터로 교체 (캐시 없으면 즉시 번역)
     */
    private void replaceWithCachedTranslations(HomeResponseDto homeData, String targetLang) {
        log.info("💾 Using cached translations for static data...");
        
        try {
            // 티저 데이터 번역
            if (homeData.getTeaser() != null) {
                TeaserDto originalTeaser = homeData.getTeaser();
                TeaserDto translatedTeaser = translateTeaserIfNeeded(originalTeaser, targetLang);
                
                // 번역된 결과를 직접 적용 (번역 유효성은 이미 translateTeaserIfNeeded에서 확인됨)
                if (translatedTeaser != null && translatedTeaser != originalTeaser) {
                    homeData.setTeaser(translatedTeaser);
                    log.info("✅ Teaser translation applied to response");
                } else {
                    log.info("⚠️ Teaser translation not applied, keeping original");
                }
            }
            
            // 박스오피스 번역
            if (homeData.getBoxOfficeMovieList() != null) {
                List<MovieDto> originalBoxOffice = homeData.getBoxOfficeMovieList();
                List<MovieDto> translatedBoxOffice = translateMovieListIfNeeded(originalBoxOffice, targetLang, "BoxOffice");
                if (translatedBoxOffice != null && translatedBoxOffice != originalBoxOffice) {
                    homeData.setBoxOfficeMovieList(translatedBoxOffice);
                    log.info("✅ BoxOffice translation completed and applied ({} movies)", translatedBoxOffice.size());
                } else {
                    log.info("⚠️ BoxOffice translation failed, keeping original");
                }
            }
            
            // 일별 TOP 번역
            if (homeData.getDailyTopMovieList() != null) {
                List<MovieDto> originalDailyTop = homeData.getDailyTopMovieList();
                List<MovieDto> translatedDailyTop = translateMovieListIfNeeded(originalDailyTop, targetLang, "DailyTop");
                if (translatedDailyTop != null && translatedDailyTop != originalDailyTop) {
                    homeData.setDailyTopMovieList(translatedDailyTop);
                    log.info("✅ DailyTop translation completed and applied ({} movies)", translatedDailyTop.size());
                } else {
                    log.info("⚠️ DailyTop translation failed, keeping original");
                }
            }
            
            // 월별 TOP 번역
            if (homeData.getMonthlyTopMovieList() != null) {
                List<MovieDto> originalMonthlyTop = homeData.getMonthlyTopMovieList();
                List<MovieDto> translatedMonthlyTop = translateMovieListIfNeeded(originalMonthlyTop, targetLang, "MonthlyTop");
                if (translatedMonthlyTop != null && translatedMonthlyTop != originalMonthlyTop) {
                    homeData.setMonthlyTopMovieList(translatedMonthlyTop);
                    log.info("✅ MonthlyTop translation completed and applied ({} movies)", translatedMonthlyTop.size());
                } else {
                    log.info("⚠️ MonthlyTop translation failed, keeping original");
                }
            }
            
            // 추천 번역
            if (homeData.getRecommendedMovieList() != null) {
                List<MovieDto> originalRecommended = homeData.getRecommendedMovieList();
                List<MovieDto> translatedRecommended = translateMovieListIfNeeded(originalRecommended, targetLang, "Recommended");
                if (translatedRecommended != null && translatedRecommended != originalRecommended) {
                    homeData.setRecommendedMovieList(translatedRecommended);
                    log.info("✅ Recommended translation completed and applied ({} movies)", translatedRecommended.size());
                } else {
                    log.info("⚠️ Recommended translation failed, keeping original");
                }
            }
            
        } catch (Exception e) {
            log.warn("⚠️ Failed to translate static data: {}", e.getMessage());
        }
    }
    
    /**
     * 티저 번역 (캐시 우선, 없으면 즉시 번역)
     */
    private TeaserDto translateTeaserIfNeeded(TeaserDto originalTeaser, String targetLang) {
        try {
            // 캐시 시도
            TeaserDto cachedTeaser = cachedTranslationService.getTranslatedTeaser(originalTeaser, targetLang);
            
            // 캐시된 데이터가 번역되었는지 확인 (더 정확한 검증)
            if (isTranslationValid(cachedTeaser, originalTeaser)) {
                log.info("📦 Using cached teaser translation");
                return cachedTeaser;
            }
            
            // 캐시 없으면 즉시 번역
            log.info("⚡ Cache miss - translating teaser in real-time");
            TeaserDto realTimeTranslated = translateTeaserRealTime(originalTeaser, targetLang);
            
            // 실시간 번역 결과를 캐시에 저장 (직렬화 에러 방지)
            try {
                if (realTimeTranslated != originalTeaser) {
                    cachedTranslationService.putTranslatedTeaser(realTimeTranslated, targetLang);
                    log.info("💾 Real-time teaser translation cached");
                }
            } catch (Exception cacheError) {
                log.warn("⚠️ Failed to cache teaser translation: {}", cacheError.getMessage());
                // 캐시 실패해도 번역된 결과는 반환
            }
            
            // 번역이 실제로 되었는지 확인하고 반환
            if (isTranslationValid(realTimeTranslated, originalTeaser)) {
                log.info("✅ Teaser real-time translation successful and will be applied");
                return realTimeTranslated;
            } else {
                log.warn("⚠️ Teaser real-time translation failed, returning original");
                return originalTeaser;
            }
            
        } catch (Exception e) {
            log.warn("⚠️ Teaser translation failed, returning original: {}", e.getMessage());
            return originalTeaser;
        }
    }
    
    /**
     * 영화 목록 번역 (캐시 우선, 없으면 즉시 번역)
     */
    private List<MovieDto> translateMovieListIfNeeded(List<MovieDto> originalMovies, String targetLang, String listType) {
        try {
            // 캐시 시도
            List<MovieDto> cachedMovies = getCachedMovieList(originalMovies, targetLang, listType);
            
            // 캐시된 데이터가 번역되었는지 확인 (더 정확한 검증)
            if (isMovieListTranslationValid(cachedMovies, originalMovies)) {
                log.info("📦 Using cached {} translation", listType);
                return cachedMovies;
            }
            
            // 캐시 없으면 즉시 번역 후 캐시에 저장
            log.info("⚡ Cache miss - translating {} in real-time", listType);
            return translateAndCacheMovieList(originalMovies, targetLang, listType);
            
        } catch (Exception e) {
            log.warn("⚠️ {} translation failed, returning original: {}", listType, e.getMessage());
            return originalMovies;
        }
    }
    
    /**
     * 캐시에서 영화 목록 가져오기
     */
    private List<MovieDto> getCachedMovieList(List<MovieDto> originalMovies, String targetLang, String listType) {
        return switch (listType) {
            case "BoxOffice" -> cachedTranslationService.getTranslatedBoxOfficeMovies(originalMovies, targetLang);
            case "DailyTop" -> cachedTranslationService.getTranslatedDailyTopMovies(originalMovies, targetLang);
            case "MonthlyTop" -> cachedTranslationService.getTranslatedMonthlyTopMovies(originalMovies, targetLang);
            case "Recommended" -> cachedTranslationService.getTranslatedRecommendedMovies(originalMovies, targetLang);
            default -> originalMovies;
        };
    }
    
    /**
     * 티저 실시간 번역
     */
    private TeaserDto translateTeaserRealTime(TeaserDto originalTeaser, String targetLang) {
        try {
            // 원본 복사본 생성 (원본 수정 방지)
            TeaserDto translatedTeaser = copyTeaser(originalTeaser);
            
            List<String> textsToTranslate = new ArrayList<>();
            List<TextReference> textReferences = new ArrayList<>();
            
            extractTextsFromTeaser(translatedTeaser, textsToTranslate, textReferences);
            
            if (textsToTranslate.isEmpty()) {
                return originalTeaser;
            }
            
            List<String> translatedTexts = libreTranslateService.translateBatch(textsToTranslate, targetLang);
            for (int i = 0; i < textReferences.size() && i < translatedTexts.size(); i++) {
                textReferences.get(i).setValue(translatedTexts.get(i));
            }
            
            // 번역 성공 시 캐시에 저장
            cachedTranslationService.putTranslatedTeaser(translatedTeaser, targetLang);
            log.info("✅ Teaser translated and cached for language: {}", targetLang);
            
            return translatedTeaser;
            
        } catch (Exception e) {
            log.error("❌ Failed to translate teaser: {}", e.getMessage());
            return originalTeaser;
        }
    }
    
    /**
     * 영화 목록 실시간 번역
     */
    private List<MovieDto> translateMovieListRealTime(List<MovieDto> originalMovies, String targetLang) {
        try {
            // 원본 복사본 생성 (원본 수정 방지)  
            List<MovieDto> translatedMovies = copyMovieList(originalMovies);
            
            List<String> textsToTranslate = new ArrayList<>();
            List<TextReference> textReferences = new ArrayList<>();
            
            extractTextsFromMovieList(translatedMovies, textsToTranslate, textReferences);
            
            if (textsToTranslate.isEmpty()) {
                return originalMovies;
            }
            
            List<String> translatedTexts = libreTranslateService.translateBatch(textsToTranslate, targetLang);
            for (int i = 0; i < textReferences.size() && i < translatedTexts.size(); i++) {
                textReferences.get(i).setValue(translatedTexts.get(i));
            }
            
            log.info("✅ Movie list translated in real-time ({} movies)", translatedMovies.size());
            return translatedMovies;
            
        } catch (Exception e) {
            log.error("❌ Failed to translate movie list: {}", e.getMessage());
            return originalMovies;
        }
    }
    
    /**
     * 영화 목록 실시간 번역 후 캐시 저장
     */
    private List<MovieDto> translateAndCacheMovieList(List<MovieDto> originalMovies, String targetLang, String listType) {
        List<MovieDto> translatedMovies = translateMovieListRealTime(originalMovies, targetLang);
        
        if (translatedMovies != originalMovies) { // 번역이 성공한 경우
            // 리스트 타입에 따라 적절한 캐시에 저장
            switch (listType) {
                case "BoxOffice" -> cachedTranslationService.putTranslatedBoxOfficeMovies(translatedMovies, targetLang);
                case "DailyTop" -> cachedTranslationService.putTranslatedDailyTopMovies(translatedMovies, targetLang);
                case "MonthlyTop" -> cachedTranslationService.putTranslatedMonthlyTopMovies(translatedMovies, targetLang);
                case "Recommended" -> cachedTranslationService.putTranslatedRecommendedMovies(translatedMovies, targetLang);
            }
            log.info("✅ {} movie list translated and cached for language: {}", listType, targetLang);
        }
        
        return translatedMovies;
    }
    
    /**
     * TeaserDto 복사본 생성
     */
    private TeaserDto copyTeaser(TeaserDto original) {
        TeaserDto copy = new TeaserDto();
        copy.setMovieId(original.getMovieId());
        copy.setTitle(original.getTitle());
        copy.setTeaserUrl(original.getTeaserUrl());
        copy.setPlot(original.getPlot());
        copy.setStillCutUrl(original.getStillCutUrl());
        copy.setReleaseDate(original.getReleaseDate());
        copy.setRunningTime(original.getRunningTime());
        copy.setGenres(original.getGenres() != null ? new ArrayList<>(original.getGenres()) : null);
        return copy;
    }
    
    /**
     * MovieDto 목록 복사본 생성
     */
    private List<MovieDto> copyMovieList(List<MovieDto> originalList) {
        return originalList.stream()
                .map(this::copyMovie)
                .toList();
    }
    
    /**
     * MovieDto 복사본 생성
     */
    private MovieDto copyMovie(MovieDto original) {
        MovieDto copy = new MovieDto();
        copy.setMovieId(original.getMovieId());
        copy.setTitle(original.getTitle());
        copy.setPlot(original.getPlot());
        copy.setReleaseDate(original.getReleaseDate());
        copy.setRunningTime(original.getRunningTime());
        copy.setAgeRating(original.getAgeRating());
        copy.setGenres(original.getGenres() != null ? new ArrayList<>(original.getGenres()) : null);
        copy.setStillCutUrl(original.getStillCutUrl());
        copy.setPosterUrl(original.getPosterUrl());
        return copy;
    }
    
    /**
     * 티저 번역이 유효한지 확인
     */
    private boolean isTranslationValid(TeaserDto cached, TeaserDto original) {
        if (cached == null || original == null) {
            return false;
        }
        
        // title이 번역되었는지 확인
        boolean titleTranslated = false;
        if (cached.getTitle() != null && original.getTitle() != null) {
            if (!cached.getTitle().equals(original.getTitle()) && 
                containsEnglishChars(cached.getTitle())) {
                titleTranslated = true;
            }
        }
        
        // plot이 번역되었는지 확인
        boolean plotTranslated = false;
        if (cached.getPlot() != null && original.getPlot() != null) {
            if (!cached.getPlot().equals(original.getPlot()) && 
                containsEnglishChars(cached.getPlot())) {
                plotTranslated = true;
            }
        }
        
        // genres가 번역되었는지 확인
        boolean genresTranslated = false;
        if (cached.getGenres() != null && original.getGenres() != null) {
            if (!cached.getGenres().equals(original.getGenres()) && 
                cached.getGenres().stream().anyMatch(this::containsEnglishChars)) {
                genresTranslated = true;
            }
        }
        
        log.info("🔍 Teaser translation validation - Title: {}, Plot: {}, Genres: {}", 
                titleTranslated, plotTranslated, genresTranslated);
        
        return titleTranslated || plotTranslated || genresTranslated;
    }
    
    /**
     * 영화 목록 번역이 유효한지 확인
     */
    private boolean isMovieListTranslationValid(List<MovieDto> cached, List<MovieDto> original) {
        if (cached == null || cached.isEmpty() || original == null || original.isEmpty()) {
            return false;
        }
        
        // 첫 번째 영화의 title이 번역되었는지 확인
        MovieDto cachedFirst = cached.get(0);
        MovieDto originalFirst = original.get(0);
        
        if (cachedFirst.getTitle() != null && originalFirst.getTitle() != null) {
            if (!cachedFirst.getTitle().equals(originalFirst.getTitle()) && 
                containsEnglishChars(cachedFirst.getTitle())) {
                return true;
            }
        }
        
        // plot이 번역되었는지 확인
        if (cachedFirst.getPlot() != null && originalFirst.getPlot() != null) {
            if (!cachedFirst.getPlot().equals(originalFirst.getPlot()) && 
                containsEnglishChars(cachedFirst.getPlot())) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * 리뷰 목록 번역이 유효한지 확인
     */
    private boolean isReviewListTranslationValid(List<ReviewDto> cached, List<ReviewDto> original) {
        if (cached == null || cached.isEmpty() || original == null || original.isEmpty()) {
            return false;
        }

        // 첫 번째 리뷰의 reviewTitle이 번역되었는지 확인
        ReviewDto cachedFirst = cached.get(0);
        ReviewDto originalFirst = original.get(0);

        if (cachedFirst.getReviewTitle() != null && originalFirst.getReviewTitle() != null) {
            if (!cachedFirst.getReviewTitle().equals(originalFirst.getReviewTitle()) &&
                containsEnglishChars(cachedFirst.getReviewTitle())) {
                return true;
            }
        }

        // content가 번역되었는지 확인
        if (cachedFirst.getContent() != null && originalFirst.getContent() != null) {
            if (!cachedFirst.getContent().equals(originalFirst.getContent()) &&
                containsEnglishChars(cachedFirst.getContent())) {
                return true;
            }
        }

        return false;
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
     * 개인화 데이터 실시간 번역 (캐시 사용 금지)
     * topLikeReviewList, topPickMovieList는 항상 실시간 번역만 한다.
     */
    private void translatePersonalizedData(HomeResponseDto homeData, String targetLang) {
        log.info("⚡ Real-time translation for personalized data...");
        try {
            // 1. topPickMovieList(영화) 캐시 확인 후 번역
            if (homeData.getTopPickMovieList() != null && !homeData.getTopPickMovieList().isEmpty()) {
                List<MovieDto> cachedMovies = cachedTranslationService.getTranslatedTopPickMovies(homeData.getTopPickMovieList(), targetLang);
                if (isMovieListTranslationValid(cachedMovies, homeData.getTopPickMovieList())) {
                    log.info("📦 Using cached TopPickMovies translation");
                    homeData.setTopPickMovieList(cachedMovies);
                } else {
                    log.info("⚡ Cache miss - translating TopPickMovies in real-time");
                    List<String> movieTexts = new ArrayList<>();
                    List<TextReference> movieReferences = new ArrayList<>();
                    extractTextsFromMovieList(homeData.getTopPickMovieList(), movieTexts, movieReferences);
                    if (!movieTexts.isEmpty()) {
                        List<String> translatedMovieTexts = libreTranslateService.translateBatch(movieTexts, targetLang);
                        for (int i = 0; i < movieReferences.size() && i < translatedMovieTexts.size(); i++) {
                            movieReferences.get(i).setValue(translatedMovieTexts.get(i));
                        }
                        log.info("✅ TopPickMovieList translated in real-time ({} movies, {} texts)", 
                                 homeData.getTopPickMovieList().size(), movieTexts.size());
                    }
                }
            }
            
            // 2. topLikeReviewList(리뷰) 캐시 확인 후 번역
            if (homeData.getTopLikeReviewList() != null && !homeData.getTopLikeReviewList().isEmpty()) {
                List<ReviewDto> cachedReviews = cachedTranslationService.getTranslatedTopLikeReviews(homeData.getTopLikeReviewList(), targetLang);
                if (isReviewListTranslationValid(cachedReviews, homeData.getTopLikeReviewList())) {
                    log.info("📦 Using cached TopLikeReviews translation");
                    homeData.setTopLikeReviewList(cachedReviews);
                } else {
                    log.info("⚡ Cache miss - translating TopLikeReviews in real-time");
                    List<String> reviewTexts = new ArrayList<>();
                    List<TextReference> reviewReferences = new ArrayList<>();
                    for (ReviewDto review : homeData.getTopLikeReviewList()) {
                        extractTextsFromReview(review, reviewTexts, reviewReferences);
                    }
                    if (!reviewTexts.isEmpty()) {
                        List<String> translatedReviewTexts = libreTranslateService.translateBatch(reviewTexts, targetLang);
                        for (int i = 0; i < reviewReferences.size() && i < translatedReviewTexts.size(); i++) {
                            reviewReferences.get(i).setValue(translatedReviewTexts.get(i));
                        }
                        log.info("✅ TopLikeReviewList translated in real-time ({} reviews, {} texts)", 
                                 homeData.getTopLikeReviewList().size(), reviewTexts.size());
                    }
                }
            }
        } catch (Exception e) {
            log.warn("⚠️ Failed to translate personalized data: {}", e.getMessage());
        }
    }
    

    
    /**
     * 텍스트 참조를 관리하는 헬퍼 클래스 (리플렉션 기반)
     */
    private static class TextReference {
        private final Object target;
        private final String fieldName;

        public TextReference(Object target, String fieldName) {
            this.target = target;
            this.fieldName = fieldName;
        }

        public void setValue(String value) {
            try {
                if (target instanceof List && fieldName.matches("\\d+")) {
                    int idx = Integer.parseInt(fieldName);
                    ((List<String>) target).set(idx, value);
                    log.info("리뷰 번역 적용 (List): {}[{}] -> {}", target.getClass().getSimpleName(), idx, value);
                } else {
                    Field field = target.getClass().getDeclaredField(fieldName);
                    field.setAccessible(true);
                    field.set(target, value);
                    log.info("리뷰 번역 적용: {}.{} -> {}", target.getClass().getSimpleName(), fieldName, value);
                }
            } catch (Exception e) {
                log.warn("리뷰 번역 setValue 실패: {}.{} / {}", target.getClass().getSimpleName(), fieldName, e.getMessage());
            }
        }
    }

    // 아래 텍스트 추출 부분도 setter 대신 리플렉션 기반으로 변경
    private void extractTextsFromTeaser(TeaserDto teaser, List<String> textsToTranslate, List<TextReference> textReferences) {
        if (teaser.getTitle() != null && !teaser.getTitle().trim().isEmpty()) {
            textsToTranslate.add(teaser.getTitle());
            textReferences.add(new TextReference(teaser, "title"));
        }
        if (teaser.getPlot() != null && !teaser.getPlot().trim().isEmpty()) {
            textsToTranslate.add(teaser.getPlot());
            textReferences.add(new TextReference(teaser, "plot"));
        }
        if (teaser.getGenres() != null) {
            for (int i = 0; i < teaser.getGenres().size(); i++) {
                String genre = teaser.getGenres().get(i);
                if (genre != null && !genre.trim().isEmpty()) {
                    textsToTranslate.add(genre);
                    textReferences.add(new TextReference(teaser.getGenres(), String.valueOf(i)));
                }
            }
        }
    }
    
    private void extractTextsFromReview(ReviewDto review, List<String> textsToTranslate, List<TextReference> textReferences) {
        if (review.getReviewTitle() != null && !review.getReviewTitle().trim().isEmpty()) {
            textsToTranslate.add(review.getReviewTitle());
            textReferences.add(new TextReference(review, "reviewTitle"));
        }
        if (review.getContent() != null && !review.getContent().trim().isEmpty()) {
            textsToTranslate.add(review.getContent());
            textReferences.add(new TextReference(review, "content"));
        }
        if (review.getMovieTitle() != null && !review.getMovieTitle().trim().isEmpty()) {
            textsToTranslate.add(review.getMovieTitle());
            textReferences.add(new TextReference(review, "movieTitle"));
        }
        if (review.getPlot() != null && !review.getPlot().trim().isEmpty()) {
            textsToTranslate.add(review.getPlot());
            textReferences.add(new TextReference(review, "plot"));
        }
        if (review.getAgeRating() != null && !review.getAgeRating().trim().isEmpty()) {
            textsToTranslate.add(review.getAgeRating());
            textReferences.add(new TextReference(review, "ageRating"));
        }
        if (review.getGenres() != null) {
            for (int i = 0; i < review.getGenres().size(); i++) {
                String genre = review.getGenres().get(i);
                if (genre != null && !genre.trim().isEmpty()) {
                    textsToTranslate.add(genre);
                    textReferences.add(new TextReference(review.getGenres(), String.valueOf(i)));
                }
            }
        }
    }
    
    private void extractTextsFromMovieList(List<MovieDto> movies, List<String> textsToTranslate, List<TextReference> textReferences) {
        if (movies == null) return;
        
        for (MovieDto movie : movies) {
            if (movie.getTitle() != null && !movie.getTitle().trim().isEmpty()) {
                textsToTranslate.add(movie.getTitle());
                textReferences.add(new TextReference(movie, "title"));
            }
            if (movie.getPlot() != null && !movie.getPlot().trim().isEmpty()) {
                textsToTranslate.add(movie.getPlot());
                textReferences.add(new TextReference(movie, "plot"));
            }
            if (movie.getAgeRating() != null && !movie.getAgeRating().trim().isEmpty()) {
                textsToTranslate.add(movie.getAgeRating());
                textReferences.add(new TextReference(movie, "ageRating"));
            }
            if (movie.getGenres() != null) {
                for (int i = 0; i < movie.getGenres().size(); i++) {
                    String genre = movie.getGenres().get(i);
                    if (genre != null && !genre.trim().isEmpty()) {
                        textsToTranslate.add(genre);
                        textReferences.add(new TextReference(movie.getGenres(), String.valueOf(i)));
                    }
                }
            }
        }
    }
}
