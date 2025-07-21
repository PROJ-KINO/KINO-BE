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
import java.util.Arrays;

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

    /**
     * 배치 번역: 여러 텍스트를 한 번의 API 호출로 번역
     * @param texts 번역할 텍스트 목록
     * @param targetLang 대상 언어
     * @return 번역된 텍스트 목록
     */
    public List<String> translateBatch(List<String> texts, String targetLang) {
        if (texts == null || texts.isEmpty()) {
            return List.of();
        }
        
        // 빈 텍스트 필터링
        List<String> filteredTexts = texts.stream()
                .filter(text -> text != null && !text.trim().isEmpty())
                .toList();
                
        if (filteredTexts.isEmpty()) {
            return texts; // 원본 그대로 반환
        }
        
        // 구분자로 텍스트들을 하나로 합치기
        String separator = "|||DEEPL_SEPARATOR|||";
        String combinedText = String.join(separator, filteredTexts);
        
        // 배치 번역 수행
        String translatedCombined = translate(combinedText, targetLang);
        
        // 결과를 다시 분리
        String[] translatedArray = translatedCombined.split("\\|\\|\\|DEEPL_SEPARATOR\\|\\|\\|");
        List<String> translatedTexts = Arrays.asList(translatedArray);
        
        // 원본과 개수가 맞지 않으면 원본 반환 (안전장치)
        if (translatedTexts.size() != filteredTexts.size()) {
            return texts;
        }
        
        // 원본 리스트 순서에 맞춰 번역 결과 매핑
        List<String> result = texts.stream()
                .map(originalText -> {
                    if (originalText == null || originalText.trim().isEmpty()) {
                        return originalText; // 빈 텍스트는 그대로
                    }
                    int index = filteredTexts.indexOf(originalText);
                    return index >= 0 && index < translatedTexts.size() 
                            ? translatedTexts.get(index).trim() 
                            : originalText;
                })
                .toList();
                
        return result;
    }
}
