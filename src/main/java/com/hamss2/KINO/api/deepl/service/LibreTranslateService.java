package com.hamss2.KINO.api.deepl.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;

import java.util.*;

@Service
public class LibreTranslateService {
    private final RestTemplate restTemplate = new RestTemplate();
    private final String apiUrl;

    public LibreTranslateService(@Value("${libretranslate.api-url}") String apiUrl) {
        this.apiUrl = apiUrl;
    }

    // 기존 DeeplService와 호환되는 2개 파라미터 버전
    public String translate(String text, String targetLang) {
        System.out.println("[LibreTranslate] 번역 요청: '" + text + "' -> '" + targetLang + "'");
        if (text == null || text.trim().isEmpty()) return text;
        
        // HTML 태그가 포함되어 있는지 확인
        if (containsHtmlTags(text)) {
            return translateHtml(text, targetLang);
        }
        
        String sourceLang = containsKorean(text) ? "ko" : "auto";
        return translatePlainText(text, sourceLang, targetLang);
    }

    // 여러 텍스트(배치) 번역 - 기존 DeeplService와 호환
    public List<String> translateBatch(List<String> texts, String targetLang) {
        System.out.println("[LibreTranslate] 배치 번역 요청: " + (texts == null ? 0 : texts.size()) + "개 -> '" + targetLang + "'");
        if (texts == null || texts.isEmpty()) return Collections.emptyList();
        List<String> result = new ArrayList<>();
        for (String text : texts) {
            result.add(translate(text, targetLang));
        }
        return result;
    }

    // 3개 파라미터 버전 (원본)
    public String translate(String text, String sourceLang, String targetLang) {
        System.out.println("[LibreTranslate] 번역(상세) 요청: '" + text + "' (" + sourceLang + ") -> '" + targetLang + "'");
        if (text == null || text.trim().isEmpty()) return text;
        
        // HTML 태그가 포함되어 있는지 확인
        if (containsHtmlTags(text)) {
            return translateHtml(text, sourceLang, targetLang);
        }
        
        return translatePlainText(text, sourceLang, targetLang);
    }

    // HTML 번역 (Jsoup 사용)
    private String translateHtml(String html, String targetLang) {
        return translateHtml(html, containsKorean(html) ? "ko" : "auto", targetLang);
    }

    private String translateHtml(String html, String sourceLang, String targetLang) {
        System.out.println("[LibreTranslate] HTML 번역 시작");
        
        try {
            Document doc = Jsoup.parse(html);
            
            // HTML 출력 시 개행과 들여쓰기 제거
            doc.outputSettings().prettyPrint(false);
            doc.outputSettings().indentAmount(0);
            
            // 번역할 텍스트 요소들 선택 (p, span, div, h1-h6 등)
            Elements textElements = doc.select("p, span, div, h1, h2, h3, h4, h5, h6, li, td, th, a, strong, em, b, i");
            
            for (Element element : textElements) {
                String originalText = element.text().trim();
                if (!originalText.isEmpty() && containsKorean(originalText)) {
                    String translatedText = translatePlainText(originalText, sourceLang, targetLang);
                    if (!translatedText.equals(originalText)) {
                        element.text(translatedText);
                        System.out.println("[LibreTranslate] HTML 요소 번역: '" + originalText + "' -> '" + translatedText + "'");
                    }
                }
            }
            
            String result = doc.body().html();
            System.out.println("[LibreTranslate] HTML 번역 완료");
            return result;
            
        } catch (Exception e) {
            System.out.println("[LibreTranslate] HTML 번역 실패: " + e.getMessage());
            return html; // 실패 시 원본 반환
        }
    }

    // 일반 텍스트 번역
    private String translatePlainText(String text, String sourceLang, String targetLang) {
        if (text == null || text.trim().isEmpty()) return text;
        
        Map<String, String> request = new HashMap<>();
        request.put("q", text);
        request.put("source", sourceLang);
        request.put("target", targetLang);
        request.put("format", "text");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, entity, Map.class);
            Object result = response.getBody().get("translatedText");
            String translatedText = result != null ? result.toString() : text;
            
            System.out.println("[LibreTranslate] 텍스트 번역 응답: '" + translatedText + "'");
            return translatedText;
        } catch (Exception e) {
            System.out.println("[LibreTranslate] 텍스트 번역 실패: " + e.getMessage());
            return text;
        }
    }

    // HTML 태그 포함 여부 확인
    private boolean containsHtmlTags(String text) {
        if (text == null) return false;
        return text.matches(".*<[^>]+>.*");
    }

    // 한국어 감지 헬퍼 메서드
    private boolean containsKorean(String text) {
        if (text == null) return false;
        return text.matches(".*[가-힣].*");
    }
} 