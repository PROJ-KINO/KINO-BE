package com.hamss2.KINO.api.deepl.advice;

import com.hamss2.KINO.api.deepl.annotation.Translate;
import com.hamss2.KINO.api.deepl.service.DeeplService;
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

@ControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class TranslationResponseAdvice implements ResponseBodyAdvice<Object> {

    private final DeeplService deeplService;

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
                    String translated = deeplService.translate(s, targetLang);
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
     * 홈 API 전용 배치 번역 메소드
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
            log.info("🏠 Starting batch translation for home data...");
            
            // 1. 모든 번역 대상 텍스트 추출
            List<String> textsToTranslate = new ArrayList<>();
            List<TextReference> textReferences = new ArrayList<>();
            
            extractTextsFromHome(homeData, textsToTranslate, textReferences);
            
            log.info("📝 Extracted {} texts for batch translation", textsToTranslate.size());
            
            // 2. 배치 번역 수행
            List<String> translatedTexts = deeplService.translateBatch(textsToTranslate, targetLang);
            
            // 3. 번역 결과를 다시 객체에 할당
            for (int i = 0; i < textReferences.size() && i < translatedTexts.size(); i++) {
                textReferences.get(i).setValue(translatedTexts.get(i));
            }
            
            log.info("✅ Batch translation completed for home data");
            return body;
            
        } catch (Exception e) {
            log.error("❌ Batch translation failed, falling back to original: {}", e.getMessage());
            return body; // 실패시 원본 반환
        }
    }
    
    /**
     * 홈 데이터에서 번역 대상 텍스트들을 추출
     */
    private void extractTextsFromHome(HomeResponseDto homeData, List<String> textsToTranslate, List<TextReference> textReferences) {
        // 1. 티저 영화
        if (homeData.getTeaser() != null) {
            extractTextsFromTeaser(homeData.getTeaser(), textsToTranslate, textReferences);
        }
        
        // 2. 리뷰 목록
        if (homeData.getTopLikeReviewList() != null) {
            for (ReviewDto review : homeData.getTopLikeReviewList()) {
                extractTextsFromReview(review, textsToTranslate, textReferences);
            }
        }
        
        // 3. 영화 목록들
        extractTextsFromMovieList(homeData.getTopPickMovieList(), textsToTranslate, textReferences);
        extractTextsFromMovieList(homeData.getBoxOfficeMovieList(), textsToTranslate, textReferences);
        extractTextsFromMovieList(homeData.getDailyTopMovieList(), textsToTranslate, textReferences);
        extractTextsFromMovieList(homeData.getMonthlyTopMovieList(), textsToTranslate, textReferences);
        extractTextsFromMovieList(homeData.getRecommendedMovieList(), textsToTranslate, textReferences);
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
                } else {
                    Field field = target.getClass().getDeclaredField(fieldName);
                    field.setAccessible(true);
                    field.set(target, value);
                }
            } catch (Exception e) {
                // 무시 또는 로그
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
