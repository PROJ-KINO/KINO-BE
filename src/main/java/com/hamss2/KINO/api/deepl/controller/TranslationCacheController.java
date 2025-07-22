package com.hamss2.KINO.api.deepl.controller;

import com.hamss2.KINO.api.deepl.scheduler.TranslationCacheScheduler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/admin/translation-cache")
public class TranslationCacheController {

    private final TranslationCacheScheduler translationCacheScheduler;

    /**
     * 번역 캐시 수동 갱신
     */
    @PostMapping("/refresh")
    public ResponseEntity<String> refreshTranslationCache() {
        log.info("🔄 수동 번역 캐시 갱신 요청");
        
        try {
            translationCacheScheduler.manualRefreshCache();
            return ResponseEntity.ok("번역 캐시 갱신이 완료되었습니다.");
        } catch (Exception e) {
            log.error("❌ 수동 번역 캐시 갱신 실패: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("번역 캐시 갱신에 실패했습니다: " + e.getMessage());
        }
    }
} 