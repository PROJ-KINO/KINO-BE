package com.hamss2.KINO.api.cache.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hamss2.KINO.api.movieAdmin.repository.MovieRepository;
import com.hamss2.KINO.api.searchMovie.dto.MovieResDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CacheRefreshService {

    private final MovieRepository movieRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${cache.key.all-movies}")
    private String cacheKey;

    public void refreshMovieCache(){
        List<MovieResDto> movies = movieRepository.findAllWithGenres().stream()
                .map(m -> new MovieResDto(
                        m.getTitle(),
                        m.getMovieId(),
                        m.getPosterUrl(),
                        m.getMovieGenres().stream()
                                .map(mg -> mg.getGenre().getGenreId())
                                .distinct()
                                .toList()
                )).toList();
        try {
            String json = objectMapper.writeValueAsString(movies);
            redisTemplate.opsForValue().set(cacheKey, json);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("영화 캐시 저장 중 JSON 변환 오류", e);
        }

        log.info("✅ 영화 캐시 갱신 완료: {}개 저장", movies.size());
    }

    public List<MovieResDto> getMoviesFromCache() {
        String json = redisTemplate.opsForValue().get(cacheKey);

        if (json == null) return null;

        try {
            return objectMapper.readValue(json, new TypeReference<List<MovieResDto>>() {});
        } catch (JsonProcessingException e) {
            log.error("🔄 JSON 파싱 실패, 캐시 재생성 시도: {}", e.getMessage());
            
            // 파싱 실패 시 캐시를 삭제하고 재생성
            redisTemplate.delete(cacheKey);
            log.info("❌ 손상된 캐시 삭제 완료");
            
            throw new IllegalStateException("영화 캐시 조회 중 JSON 파싱 오류", e);
        }
    }
}
