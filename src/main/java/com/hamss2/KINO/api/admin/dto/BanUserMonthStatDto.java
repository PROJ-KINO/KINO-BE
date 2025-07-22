package com.hamss2.KINO.api.admin.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BanUserMonthStatDto {
    private String month;   // ex) "2025-07"
    private int banCount;   // 정지 회원 수

    public BanUserMonthStatDto(String month, int banCount) {
        this.month = month;
        this.banCount = banCount;
    }
}
