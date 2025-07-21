package com.hamss2.KINO.api.deepl.advice;

import com.hamss2.KINO.api.deepl.annotation.Translate;
import com.hamss2.KINO.api.deepl.service.DeeplService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.lang.reflect.Field;
import java.util.*;

@ControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class TranslationResponseAdvice implements ResponseBodyAdvice<Object> {

    private final DeeplService deeplService;

    @Override
    public boolean supports(MethodParameter returnType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        log.info("supports() called: converterType={}", converterType);

        return returnType.getContainingClass().isAnnotationPresent(Translate.class)
                || returnType.hasMethodAnnotation(Translate.class);
    }

    @Override
    public Object beforeBodyWrite(Object body,
                                  MethodParameter returnType,
                                  MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request,
                                  ServerHttpResponse response) {
        if (body == null) return null;
        // 번역 타겟 언어 추출
        List<String> langs = request.getHeaders().getOrDefault("X-Target-Lang",
                request.getHeaders().getOrDefault("Accept-Language", List.of("EN")));
        String targetLang = langs.get(0).toUpperCase();
        if(targetLang.equals("KO-KR,KO;Q=0.9,EN-US;Q=0.8,EN;Q=0.7") || targetLang.equals("KO")) return body;
        log.info("============================== targetLang : " + targetLang + "=============================");
        
        // 순환 참조 방지를 위한 방문 기록
        Set<Object> visited = new HashSet<>();
        translateFields(body, targetLang, visited);
        return body;
    }

    private void translateFields(Object obj, String targetLang, Set<Object> visited) {
        if (obj == null) return;
        
        // 순환 참조 방지: 이미 방문한 객체는 스킵
        if (visited.contains(obj)) {
            log.info("🔄 Circular reference detected, skipping: {}", obj.getClass().getName());
            return;
        }
        visited.add(obj);

        log.info("🔍 Analyzing object: {} (class: {})", 
                obj.toString().length() > 100 ? obj.toString().substring(0, 100) + "..." : obj.toString(), 
                obj.getClass().getName());

        if (obj instanceof List<?>) {
            log.info("📝 Found List with {} items", ((List<?>) obj).size());
            for (Object item : (List<?>) obj) translateFields(item, targetLang, visited);
            return;
        }
        if (obj instanceof java.util.Map<?,?>) {
            log.info("📝 Found Map with {} entries", ((java.util.Map<?,?>) obj).size());
            for (Object v : ((java.util.Map<?,?>) obj).values()) translateFields(v, targetLang, visited);
            return;
        }
        
        Class<?> cls = obj.getClass();
        if (isPrimitiveOrWrapper(cls) || cls.isEnum() || cls.getName().startsWith("java.lang") 
            || (cls.getName().startsWith("org.springframework") && !cls.getName().startsWith("org.springframework.data"))) {
            log.info("⚠️ Skipping class: {} (primitive/wrapper/enum/java/spring class)", cls.getName());
            return;
        }

        // 현재 클래스와 부모 클래스의 모든 필드 수집
        List<Field> allFields = new ArrayList<>();
        Class<?> currentClass = cls;
        while (currentClass != null && !currentClass.getName().startsWith("java.")) {
            allFields.addAll(Arrays.asList(currentClass.getDeclaredFields()));
            currentClass = currentClass.getSuperclass();
        }
        
        log.info("🔧 Processing {} fields in class: {} (including parent classes)", allFields.size(), cls.getName());
        for (Field f : allFields) {
            f.setAccessible(true);
            try {
                Object val = f.get(obj);
                if (val == null) continue;
                
                String fieldInfo = val.toString().length() > 50 ? val.toString().substring(0, 50) + "..." : val.toString();
                log.info("🔎 Field '{}' ({}): {}", f.getName(), f.getType().getSimpleName(), fieldInfo);
                
                if (val instanceof String s && !s.isBlank()) {
                    String translated = deeplService.translate(s, targetLang);
                    log.info("✅ Translating '{}' to '{}' -> '{}'", s, targetLang, translated);
                    f.set(obj, translated);
                } else if (val != null) {
                    log.info("🔄 Recursing into field: {}", f.getName());
                    translateFields(val, targetLang, visited);
                }
            } catch (IllegalAccessException e) {
                log.warn("❌ Cannot access field: {}", f.getName());
            } catch (Exception e) {
                log.warn("⚠️ Error processing field {}: {}", f.getName(), e.getMessage());
            }
        }
        
        // 처리 완료 후 방문 기록에서 제거 (다른 경로로 다시 방문 가능하도록)
        visited.remove(obj);
    }

    private boolean isPrimitiveOrWrapper(Class<?> cls) {
        return cls.isPrimitive()
                || cls == Boolean.class
                || cls == Byte.class
                || cls == Character.class
                || cls == Short.class
                || cls == Integer.class
                || cls == Long.class
                || cls == Float.class
                || cls == Double.class
                || cls == Void.class;
    }
}
