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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

@Service
@RequiredArgsConstructor
public class DeeplService {
    private final DeeplConfig deeplConfig;
    private final RestTemplate restTemplate = new RestTemplate();

    public String translate(String text, String targetLang) {
        // HTML 태그 보호
        Map<String, String> htmlTags = new HashMap<>();
        String protectedText = protectHtmlTags(text, htmlTags);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("auth_key", deeplConfig.getApiKey());
        params.add("text", protectedText);
        params.add("target_lang", targetLang); // 예: "KO"

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(deeplConfig.getUrl(), request, Map.class);

        List<Map<String, String>> translations = (List<Map<String, String>>) response.getBody().get("translations");

        String translatedText = translations.get(0).get("text");
        
        // HTML 태그 복원
        return restoreHtmlTags(translatedText, htmlTags);
    }

    /**
     * 배치 번역: 여러 텍스트를 청크 단위로 나누어 번역
     * @param texts 번역할 텍스트 목록
     * @param targetLang 대상 언어
     * @return 번역된 텍스트 목록
     */
    public List<String> translateBatch(List<String> texts, String targetLang) {
        if (texts == null || texts.isEmpty()) {
            return List.of();
        }
        
        // 청크 크기 설정 (API 제한을 고려하여 작게 설정)
        final int CHUNK_SIZE = 50;
        
        // 빈 텍스트 필터링 및 인덱스 매핑
        List<TextWithIndex> filteredTexts = new ArrayList<>();
        for (int i = 0; i < texts.size(); i++) {
            String text = texts.get(i);
            if (text != null && !text.trim().isEmpty()) {
                filteredTexts.add(new TextWithIndex(text, i));
            }
        }
        
        if (filteredTexts.isEmpty()) {
            return texts; // 원본 그대로 반환
        }
        
        // 결과 리스트 초기화
        List<String> result = new ArrayList<>(texts);
        
        try {
            // 청크 단위로 번역 처리
            for (int i = 0; i < filteredTexts.size(); i += CHUNK_SIZE) {
                int endIndex = Math.min(i + CHUNK_SIZE, filteredTexts.size());
                List<TextWithIndex> chunk = filteredTexts.subList(i, endIndex);
                
                // 청크 번역
                List<String> chunkTexts = chunk.stream()
                        .map(TextWithIndex::getText)
                        .toList();
                        
                List<String> translatedChunk = translateChunk(chunkTexts, targetLang);
                
                // 번역 결과를 원본 위치에 매핑
                for (int j = 0; j < chunk.size() && j < translatedChunk.size(); j++) {
                    int originalIndex = chunk.get(j).getIndex();
                    result.set(originalIndex, translatedChunk.get(j));
                }
                
                // API 호출 간격 조절 (과도한 요청 방지)
                if (endIndex < filteredTexts.size()) {
                    Thread.sleep(100); // 100ms 대기
                }
            }
        } catch (Exception e) {
            // 오류 발생 시 원본 반환
            return texts;
        }
        
        return result;
    }
    
    /**
     * 하나의 청크를 번역하는 메소드
     */
    private List<String> translateChunk(List<String> texts, String targetLang) {
        if (texts.isEmpty()) {
            return List.of();
        }
        
        // 구분자로 텍스트들을 하나로 합치기
        String separator = "|||DEEPL_SEPARATOR|||";
        String combinedText = String.join(separator, texts);
        
        // 배치 번역 수행
        String translatedCombined = translate(combinedText, targetLang);
        
        // 결과를 다시 분리
        String[] translatedArray = translatedCombined.split("\\|\\|\\|DEEPL_SEPARATOR\\|\\|\\|");
        List<String> translatedTexts = Arrays.asList(translatedArray);
        
        // 원본과 개수가 맞지 않으면 개별 번역으로 폴백
        if (translatedTexts.size() != texts.size()) {
            return texts.stream()
                    .map(text -> {
                        try {
                            return translate(text, targetLang);
                        } catch (Exception e) {
                            return text; // 실패시 원본 반환
                        }
                    })
                    .toList();
        }
        
        return translatedTexts.stream()
                .map(String::trim)
                .toList();
    }
    
    /**
     * 텍스트와 원본 인덱스를 저장하는 헬퍼 클래스
     */
    private static class TextWithIndex {
        private final String text;
        private final int index;
        
        public TextWithIndex(String text, int index) {
            this.text = text;
            this.index = index;
        }
        
        public String getText() {
            return text;
        }
        
        public int getIndex() {
            return index;
        }
    }
    
    /**
     * HTML 태그를 플레이스홀더로 대체하여 번역에서 보호
     */
    private String protectHtmlTags(String text, Map<String, String> htmlTags) {
        if (text == null || text.trim().isEmpty()) {
            return text;
        }
        
        // img 태그와 figure 블록 전체를 보호
        Pattern htmlPattern = Pattern.compile("(<figure[^>]*>.*?</figure>)|(<img[^>]*>)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher matcher = htmlPattern.matcher(text);
        
        StringBuffer protectedText = new StringBuffer();
        int tagCounter = 0;
        
        while (matcher.find()) {
            String htmlTag = matcher.group();
            String placeholder = "HTMLTAG" + tagCounter + "PLACEHOLDER";
            
            // 플레이스홀더와 원본 태그 매핑 저장
            htmlTags.put(placeholder, htmlTag);
            
            // 태그를 플레이스홀더로 대체
            matcher.appendReplacement(protectedText, placeholder);
            tagCounter++;
        }
        matcher.appendTail(protectedText);
        
        return protectedText.toString();
    }
    
    /**
     * 번역된 텍스트에서 플레이스홀더를 원래 HTML 태그로 복원
     */
    private String restoreHtmlTags(String translatedText, Map<String, String> htmlTags) {
        if (translatedText == null || htmlTags.isEmpty()) {
            return translatedText;
        }
        
        String restoredText = translatedText;
        
        // 모든 플레이스홀더를 원래 HTML 태그로 복원
        for (Map.Entry<String, String> entry : htmlTags.entrySet()) {
            String placeholder = entry.getKey();
            String originalTag = entry.getValue();
            
            restoredText = restoredText.replace(placeholder, originalTag);
        }
        
        return restoredText;
    }
}
