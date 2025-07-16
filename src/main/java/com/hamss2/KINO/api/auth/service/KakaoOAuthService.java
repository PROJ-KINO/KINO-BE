package com.hamss2.KINO.api.auth.service;

import com.hamss2.KINO.api.auth.dto.KakaoDto;
import com.hamss2.KINO.api.auth.dto.KakaoOAuthResDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class KakaoOAuthService {

    private final static String BASE_URL = "https://kauth.kakao.com/oauth";
    private final static WebClient oauthWebClient = WebClient.builder()
        .baseUrl(BASE_URL)
        .build();
    private final static WebClient apiWebClient = WebClient.builder()
        .baseUrl("https://kapi.kakao.com")
        .build();

    @Value("${kakao.client-id}")
    private String kakaoClientId;
    @Value("${kakao.redirect-uri}")
    private String kakaoRedirectUri;
    @Value("${kakao.client-secret}")
    private String kakaoClientSecret; // 카카오 클라이언트 시크릿 키

    public String getKakaoAuthUrl() {
        return UriComponentsBuilder
            .fromUriString(BASE_URL + "/authorize")
            .queryParam("response_type", "code")
            .queryParam("client_id", kakaoClientId)
            .queryParam("redirect_uri", kakaoRedirectUri)
            .queryParam("scope", "profile_nickname,profile_image,account_email")
            .build()
            .toUriString();

    }

    public KakaoOAuthResDto getKakaoAccessToken(String code) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "authorization_code");
        formData.add("client_id", kakaoClientId);
        formData.add("redirect_uri", kakaoRedirectUri); // Kakao Developers에 등록된 값과 반드시 동일
        formData.add("code", code); // 프론트에서 받은 code
        formData.add("client_secret", kakaoClientSecret);

        return oauthWebClient.post()
            .uri("https://kauth.kakao.com/oauth/token")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .bodyValue(formData)
            .retrieve()
            .bodyToMono(KakaoOAuthResDto.class) // 응답을 String으로 변환
            .block(); // 동기적으로 반환
    }

    public KakaoDto getUserInfo(String accessToken) {

        String userInfoUrl = "/v2/user/me";

        return apiWebClient.get()
            .uri(userInfoUrl)
            .headers(headers -> headers.setBearerAuth(accessToken)) // Bearer 토큰 설정
            .retrieve()
            .bodyToMono(KakaoDto.class) // 응답을 String으로 변환
            .block(); // 동기적으로 반환
    }
}
