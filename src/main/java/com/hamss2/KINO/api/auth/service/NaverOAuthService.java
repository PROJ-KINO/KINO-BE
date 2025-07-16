package com.hamss2.KINO.api.auth.service;

import com.hamss2.KINO.api.auth.dto.NaverDto;
import com.hamss2.KINO.api.auth.dto.NaverOAuthResDto;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class NaverOAuthService {

    private final static String BASE_URL = "https://nid.naver.com";
    private final static WebClient oauthWebClient = WebClient.builder()
        .baseUrl(BASE_URL)
        .build();
    private final static WebClient apiWebClient = WebClient.builder()
        .baseUrl("https://openapi.naver.com")
        .build();

    @Value("${naver.client-id}")
    private String naverClientId;
    @Value("${naver.redirect-uri}")
    private String naverRedirectUri;
    @Value("${naver.client-secret}")
    private String naverClientSecret;

    public String getNaverAuthUrl() {
        return UriComponentsBuilder
            .fromUriString(BASE_URL + "/oauth2.0/authorize")
            .queryParam("response_type", "code")
            .queryParam("client_id", naverClientId)
            .queryParam("redirect_uri", naverRedirectUri)
            // .queryParam("scope", "profile_nickname,profile_image") // 필요하면 추가
            // .queryParam("state", state) // CSRF 방지용 state도 추가 권장
            .build()
            .toUriString();
    }

    public NaverOAuthResDto issueNaverAccessToken(String code, String state) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "authorization_code");
        formData.add("client_id", naverClientId);
        formData.add("client_secret", naverClientSecret);
        formData.add("code", code); // 프론트에서 받은 code
        formData.add("state", state); // 프론트에서 받은 state

        return oauthWebClient.post()
            .uri("/oauth2.0/token")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .bodyValue(formData)
            .retrieve()
            .bodyToMono(NaverOAuthResDto.class) // 응답을 String으로 변환
            .block(); // 동기적으로 반환
    }

    //    public String getUserInfo(String accessToken) {
    public NaverDto getUserInfo(String accessToken) {
        // 사용자 정보 요청을 위한 엔드포인트
        String userInfoUrl = "/v1/nid/me";

        // 헤더 설정
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + accessToken);

        // 사용자 정보 요청
        return apiWebClient.get()
            .uri(userInfoUrl)
            .headers(httpHeaders -> httpHeaders.setAll(headers))
            .retrieve()
//            .bodyToMono(String.class) // 응답을 String으로 변환
            .bodyToMono(NaverDto.class)
            .block(); // 동기적으로 반환
    }

}

/*
grant_type
인증 과정에 대한 구분값
1. 발급:'authorization_code'
2. 갱신:'refresh_token'
3. 삭제: 'delete'
 */

/*
public ResponseEntity<?> naverCallback(
    @RequestParam String code,
    @RequestParam String state
) {
    // 1. 토큰 요청
    String tokenUrl = "https://nid.naver.com/oauth2.0/token";
    RestTemplate restTemplate = new RestTemplate();

    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add("grant_type", "authorization_code");
    params.add("client_id", clientId);
    params.add("client_secret", clientSecret);
    params.add("code", code);
    params.add("state", state);
    params.add("redirect_uri", redirectUri);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

    HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
    ResponseEntity<Map> tokenResponse = restTemplate.postForEntity(tokenUrl, request, Map.class);

    if (!tokenResponse.getStatusCode().is2xxSuccessful()) {
        return ResponseEntity.status(401).body("네이버 토큰 요청 실패");
    }

    String accessToken = (String) tokenResponse.getBody().get("access_token");

    // 2. 사용자 정보 요청
    String userInfoUrl = "https://openapi.naver.com/v1/nid/me";
    HttpHeaders userHeaders = new HttpHeaders();
    userHeaders.setBearerAuth(accessToken);
    HttpEntity<?> userRequest = new HttpEntity<>(userHeaders);

    ResponseEntity<Map> userResponse = restTemplate.exchange(userInfoUrl, HttpMethod.GET,
        userRequest, Map.class);

    if (!userResponse.getStatusCode().is2xxSuccessful()) {
        return ResponseEntity.status(401).body("네이버 유저 정보 요청 실패");
    }

    // 3. 유저 정보 처리 (여기서 회원가입/JWT 발급 등 로직)
    Map<String, Object> responseMap = (Map<String, Object>) userResponse.getBody().get("response");
    String email = (String) responseMap.get("email");
    String name = (String) responseMap.get("name");

    // TODO: 회원가입/로그인 처리 & JWT 발급 등...

    // 예시 반환
    return ResponseEntity.ok(responseMap);
}

 */