package com.hamss2.KINO.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
    // 1. KOFIC(영화진흥위원회) 전용 WebClient
    @Bean
    public WebClient koficWebClient() {
        return WebClient.builder()
                .baseUrl("http://www.kobis.or.kr")
                .build();
    }
    // 2. TMDB 전용 WebClient
    @Bean
    public WebClient tmdbWebClient() {
        return WebClient.builder()
                .baseUrl("https://api.themoviedb.org/3")
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024)) // 10MB
                .build();
    }
    // 3. Flask 서버
//    @Bean
//    public WebClient recommenderWebClient() {
//        return WebClient.builder()
//                .baseUrl("http://localhost:5001") // Flask 서버 주소
//                .build();
//    }
}
