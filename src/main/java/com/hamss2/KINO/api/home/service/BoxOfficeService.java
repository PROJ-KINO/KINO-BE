package com.hamss2.KINO.api.home.service;

import com.hamss2.KINO.api.entity.Movie;
import com.hamss2.KINO.api.home.dto.res.MovieDto;
import com.hamss2.KINO.api.movieAdmin.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BoxOfficeService {

    @Qualifier("koficWebClient")
    private final WebClient koficWebClient;

    private final MovieRepository movieRepository;
    @Value("${kofic.api-key}")
    private String KOFIC_API_KEY;

    public List<MovieDto> fetchRealBoxOfficeTop10() {
        String today = LocalDate.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        // 1. KOFIC 오픈API 호출
        Map<String, Object> result = koficWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("http")
                        .host("www.kobis.or.kr")
                        .path("/kobisopenapi/webservice/rest/boxoffice/searchDailyBoxOfficeList.json")
                        .queryParam("key", KOFIC_API_KEY)
                        .queryParam("targetDt", today)
                        .build())
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        // 2. 응답 파싱
        Map<String, Object> boxOfficeResult = (Map<String, Object>) result.get("boxOfficeResult");
        List<Map<String, Object>> dailyList = (List<Map<String, Object>>) boxOfficeResult.get("dailyBoxOfficeList");

        // 3. 영화명으로 우리 DB와 매칭해서 DTO
        List<MovieDto> movieDtos = new ArrayList<>();

        for (Map<String, Object> item : dailyList) {
            String koficTitle = (String) item.get("movieNm");
            List<Movie> movies = movieRepository.findAllByTitle(koficTitle);

            if (!movies.isEmpty()) {
                Movie movie = movies.get(0); // 제일 먼저 나온 영화 사용
                MovieDto dto = new MovieDto();
                dto.setMovieId(movie.getMovieId());
                dto.setTitle(movie.getTitle());
                dto.setPosterUrl(movie.getPosterUrl());
                movieDtos.add(dto);
            }

            if (movieDtos.size() >= 10) break;
        }
        return movieDtos;
    }
}
