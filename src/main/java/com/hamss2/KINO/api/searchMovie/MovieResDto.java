package com.hamss2.KINO.api.searchMovie;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovieResDto {
    private String title;
    private Long movieId;
    private String posterUrl;
}
