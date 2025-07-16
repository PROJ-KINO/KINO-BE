package com.hamss2.KINO.api.auth.dto;

public enum LoginType {
    KAKAO("kakao"),
    GOOGLE("google"),
    NAVER("naver");

    private final String type;

    LoginType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
