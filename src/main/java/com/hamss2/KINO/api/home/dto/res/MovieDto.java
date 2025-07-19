package com.hamss2.KINO.api.home.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class MovieDto implements Serializable {
    @JsonProperty("movie_id")
    private Long movieId;
    private String title;
    private String plot;
    @JsonProperty("release_date")
    private LocalDate releaseDate;
    @JsonProperty("running_time")
    private int runningTime;
    @JsonProperty("age_rating")
    private String ageRating;
    private List<String> genres;
    @JsonProperty("still_cut_url")
    private String stillCutUrl;
    @JsonProperty("poster_url")
    private String posterUrl;
}
