package com.hamss2.KINO.api.searchMovie.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovieResDto {
    private String title;
    private Long movieId;
    private String posterUrl;
    private List<String> movieGenre;
}
