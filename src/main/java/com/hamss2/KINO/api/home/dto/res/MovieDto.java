package com.hamss2.KINO.api.home.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class MovieDto implements Serializable {
    @JsonProperty("movie_id")
    private Long movieId;
    private String title;
    @JsonProperty("still_cut_url")
    private String posterUrl;
}
