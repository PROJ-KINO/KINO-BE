package com.hamss2.KINO.api.deepl.service;

import com.hamss2.KINO.api.deepl.config.DeeplConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DeeplService {
    private final DeeplConfig deeplConfig;
    private final RestTemplate restTemplate = new RestTemplate();

    public String translate(String text, String targetLang) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("auth_key", deeplConfig.getApiKey());
        params.add("text", text);
        params.add("target_lang", targetLang); // 예: "KO"

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(deeplConfig.getUrl(), request, Map.class);

        List<Map<String, String>> translations = (List<Map<String, String>>) response.getBody().get("translations");

        return translations.get(0).get("text");
    }
}
