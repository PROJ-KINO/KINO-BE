package com.hamss2.KINO.api.home.service;

import com.hamss2.KINO.api.entity.Movie;
import com.hamss2.KINO.api.home.dto.res.MovieDto;
import com.hamss2.KINO.api.movieAdmin.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class BoxOfficeService {

    @Qualifier("koficWebClient")
    private final WebClient koficWebClient;

    private final MovieRepository movieRepository;
    @Value("${kofic.api-key}")
    private String KOFIC_API_KEY;

    @Cacheable(value = "boxOfficeCache", key = "'top10'")
    public List<MovieDto> fetchRealBoxOfficeTop10() {
        List<Map<String, Object>> allBoxOfficeList = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

        // 1. 4일치 박스오피스 데이터 가져오기
        for (int i = 1; i <= 4; i++) {
            String day = LocalDate.now().minusDays(i).format(formatter);
            Map<String, Object> result = koficWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("http")
                            .host("www.kobis.or.kr")
                            .path("/kobisopenapi/webservice/rest/boxoffice/searchDailyBoxOfficeList.json")
                            .queryParam("key", KOFIC_API_KEY)
                            .queryParam("targetDt", day)
                            .build())
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            Map<String, Object> boxOfficeResult = (Map<String, Object>) result.get("boxOfficeResult");
            List<Map<String, Object>> dailyList = (List<Map<String, Object>>) boxOfficeResult.get("dailyBoxOfficeList");
            allBoxOfficeList.addAll(dailyList);
        }

        // 2. 영화명 기준 중복 제거 (KOFIC 영화명 중복)
        LinkedHashMap<String, Map<String, Object>> unique = new LinkedHashMap<>();
        for (Map<String, Object> item : allBoxOfficeList) {
            unique.putIfAbsent((String) item.get("movieNm"), item);
        }
        List<Map<String, Object>> topNList = new ArrayList<>(unique.values());

        // 3. 우리 DB와 매칭
        List<MovieDto> movieDtos = new ArrayList<>();
        for (Map<String, Object> item : topNList) {
            String koficTitle = (String) item.get("movieNm");
            String koficTitleWithoutSpace = koficTitle.replaceAll("\\s+", "");
            List<Movie> movies = movieRepository.findAllByTitleIgnoringSpace(koficTitleWithoutSpace);

            if (!movies.isEmpty()) {
                Movie movie = movies.get(0); // 첫번째 매칭 영화
                MovieDto dto = new MovieDto();
                dto.setMovieId(movie.getMovieId());
                dto.setTitle(movie.getTitle());
                dto.setPosterUrl(movie.getStillCutUrl());
                movieDtos.add(dto);
            }

            if (movieDtos.size() >= 10) break;
        }
        return movieDtos;
    }
}
