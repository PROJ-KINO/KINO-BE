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
import java.util.List;

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
        log.info("============================== targetLang : " + targetLang + "=============================");
        if(targetLang.equals("KO-KR,KO;Q=0.9,EN-US;Q=0.8,EN;Q=0.7")) return body;
        log.info("============================== targetLang : " + targetLang + "=============================");
        translateFields(body, targetLang);
        return body;
    }

    private void translateFields(Object obj, String targetLang) {
        if (obj == null) return;

        if (obj instanceof List<?>) {
            for (Object item : (List<?>) obj) translateFields(item, targetLang);
            return;
        }
        if (obj instanceof java.util.Map<?,?>) {
            for (Object v : ((java.util.Map<?,?>) obj).values()) translateFields(v, targetLang);
            return;
        }
        Class<?> cls = obj.getClass();
        if (isPrimitiveOrWrapper(cls) || cls.isEnum() || cls.getName().startsWith("java.")) {
            return;
        }

        for (Field f : cls.getDeclaredFields()) {
            f.setAccessible(true);
            try {
                Object val = f.get(obj);
                if (val instanceof String s && !s.isBlank()) {
                    String translated = deeplService.translate(s, targetLang);
                    log.info("Translating '{}' to '{}' -> '{}'", s, targetLang, translated);
                    f.set(obj, translated);
                } else if (val != null) {
                    translateFields(val, targetLang);
                }
            } catch (IllegalAccessException ignored) {}
        }
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
