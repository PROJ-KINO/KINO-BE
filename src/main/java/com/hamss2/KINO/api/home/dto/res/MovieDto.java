package com.hamss2.KINO.api.home.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MovieDto {
    @JsonProperty("movie_id")
    private Long movieId;
    private String title;
    @JsonProperty("poster_url")
    private String posterUrl;
}
