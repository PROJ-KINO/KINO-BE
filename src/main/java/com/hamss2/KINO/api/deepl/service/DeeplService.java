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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class DeeplService {
    private final DeeplConfig deeplConfig;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ExecutorService executorService = Executors.newFixedThreadPool(4); // 병렬 처리용

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
        params.add("ignore_tags", "figure,img");

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
        // 무조건 개별 번역만 수행
        List<String> result = new ArrayList<>();
        for (String text : texts) {
            if (text != null && !text.trim().isEmpty()) {
                try {
                    result.add(translate(text, targetLang));
                } catch (Exception e) {
                    result.add(text); // 실패시 원본 반환
                }
            } else {
                result.add(text);
            }
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
        
        // HTML이 포함된 텍스트가 있으면 개별 번역으로 처리
        boolean hasHtml = texts.stream().anyMatch(text -> text.contains("<") && text.contains(">"));
        if (hasHtml) {
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
        
        // HTML이 없는 텍스트만 배치 번역 수행
        try {
            // 더 안전한 구분자 사용
            String separator = "\n###KINO_TRANSLATE_SEP###\n";
            String combinedText = String.join(separator, texts);
            
            // 배치 번역 수행
            String translatedCombined = translate(combinedText, targetLang);
            
            // 결과를 다시 분리
            String[] translatedArray = translatedCombined.split("\n###KINO_TRANSLATE_SEP###\n");
            List<String> translatedTexts = Arrays.asList(translatedArray);
            
            // 원본과 개수가 맞지 않으면 개별 번역으로 폴백
            if (translatedTexts.size() != texts.size()) {
                throw new RuntimeException("배치 번역 결과 개수 불일치");
            }
            
            return translatedTexts.stream()
                    .map(String::trim)
                    .toList();
                    
        } catch (Exception e) {
            // 배치 번역 실패시 개별 번역으로 폴백
            return texts.stream()
                    .map(text -> {
                        try {
                            return translate(text, targetLang);
                        } catch (Exception ex) {
                            return text; // 실패시 원본 반환
                        }
                    })
                    .toList();
        }
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
     * 청크 번역 결과를 저장하는 헬퍼 클래스
     */
    private static class ChunkResult {
        private final List<TextWithIndex> chunk;
        private final List<String> translatedTexts;
        
        public ChunkResult(List<TextWithIndex> chunk, List<String> translatedTexts) {
            this.chunk = chunk;
            this.translatedTexts = translatedTexts;
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
