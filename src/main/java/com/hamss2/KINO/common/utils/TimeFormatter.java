package com.hamss2.KINO.common.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TimeFormatter {

    // yyyy.MM.dd HH:mm 형식으로 LocalDateTime을 포맷팅하는 메서드
    public static String formatLocalDateTime(LocalDateTime localDateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm");
        return localDateTime.format(formatter);
    }

}
