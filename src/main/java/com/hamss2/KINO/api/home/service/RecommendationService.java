package com.hamss2.KINO.api.home.service;

import com.hamss2.KINO.api.entity.Movie;
import com.hamss2.KINO.api.home.dto.res.MovieDto;
import com.hamss2.KINO.api.movieAdmin.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final WebClient recommenderWebClient;

//    public List<MovieDto> getRecommendations(Long userId, int n) {
//        Map<String, Object> request = new HashMap<>();
//        request.put("user_id", userId);
//        request.put("n", n);
//
//        // Flask에서 {"movies": [...]} 형태로 오기 때문에 movies 필드를 꺼내줘야 함
//        Map<String, List<MovieDto>> responseMap = recommenderWebClient.post()
//                .uri("/recommend")
//                .bodyValue(request)
//                .retrieve()
//                .bodyToMono(new ParameterizedTypeReference<Map<String, List<MovieDto>>>() {})
//                .block();
//
//        return responseMap.getOrDefault("movies", List.of());
//    }
}
