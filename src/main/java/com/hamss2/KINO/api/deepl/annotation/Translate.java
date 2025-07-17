package com.hamss2.KINO.api.deepl.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.METHOD, ElementType.TYPE })  // 클래스나 메서드에 붙일 수 있게
@Retention(RetentionPolicy.RUNTIME)               // 런타임까지 유지돼서 리플렉션으로 읽을 수 있음
public @interface Translate {
    // 옵션을 넣어도 되고 그냥 표식용으로 쓸 수도 있음
}
