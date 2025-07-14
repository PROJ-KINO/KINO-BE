package com.hamss2.KINO.api.mypage.dto;

import lombok.*;

import java.util.List;
import java.util.ArrayList;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MypageGenreReqDto {
    private List<Long> genreIds = new ArrayList<>();
    
    // null 안전성을 위한 커스텀 getter
    public List<Long> getGenreIds() {
        return genreIds != null ? genreIds : new ArrayList<>();
    }
}
