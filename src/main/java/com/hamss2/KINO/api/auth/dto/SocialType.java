package com.hamss2.KINO.api.auth.dto;

public enum SocialType {
    KAKAO("kakao"),
    GOOGLE("google"),
    NAVER("naver");

    private final String type;

    SocialType(String type) {
        this.type = type;
    }

    public static SocialType fromString(String type) {
        for (SocialType socialType : SocialType.values()) {
            if (socialType.getType().equalsIgnoreCase(type)) {
                return socialType;
            }
        }
        throw new IllegalArgumentException("Unknown social type: " + type);
    }

    public String getType() {
        return type;
    }
}
