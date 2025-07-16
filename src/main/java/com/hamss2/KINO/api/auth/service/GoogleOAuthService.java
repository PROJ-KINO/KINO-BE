package com.hamss2.KINO.api.auth.service;

import com.hamss2.KINO.api.auth.dto.GoogleOAuthResDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class GoogleOAuthService {

    private final static String BASE_URL = "https://oauth2.googleapis.com";
    private final static WebClient webClient = WebClient.builder()
        .baseUrl(BASE_URL)
        .build();

    @Value("${google.client-id}")
    private String googleClientId;
    @Value("${google.client-secret}")
    private String googleClientSecret;
    @Value("${google.redirect-uri}")
    private String googleRedirectUri;

    public String getGoogleAuthUrl() {
        return UriComponentsBuilder
            .fromUriString(BASE_URL + "/o/oauth2/v2/auth")
            .queryParam("client_id", googleClientId)
            .queryParam("redirect_uri", googleRedirectUri)
            .queryParam("response_type", "code")
            .queryParam("scope", "email profile") // 필요한 scope를 추가 email%20profile
            .queryParam("access_type", "offline") // refresh token을 원할 경우 access_type을 offline으로 설정
            .build()
            .toUriString();
    }

    public GoogleOAuthResDto issueGoogleAccessToken(String code, String state) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "authorization_code");
        formData.add("client_id", googleClientId);
        formData.add("redirect_uri", googleRedirectUri);
        formData.add("code", code);
        formData.add("client_secret", googleClientSecret);
//        formData.add("state", state);

        return webClient.post()
            .uri("https://oauth2.googleapis.com/token")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .bodyValue(formData)
            .retrieve()
            .bodyToMono(GoogleOAuthResDto.class)
            .block(); // 동기적으로 반환
    }

}