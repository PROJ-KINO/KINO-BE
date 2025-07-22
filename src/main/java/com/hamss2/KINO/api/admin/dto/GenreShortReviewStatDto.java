package com.hamss2.KINO.api.admin.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GenreShortReviewStatDto {
    private String genre;
    private int shortReviewCount;

    public GenreShortReviewStatDto(String genre, int shortReviewCount) {
        this.genre = genre;
        this.shortReviewCount = shortReviewCount;
    }
}
