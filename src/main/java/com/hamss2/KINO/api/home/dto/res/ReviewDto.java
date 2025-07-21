package com.hamss2.KINO.api.home.dto.res;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class ReviewDto {
    private Long reviewId;
    private String reviewTitle;
    private String content;
    private Long movieId;
    private String movieTitle;
    private String plot;
    private LocalDate releaseDate;
    private int runningTime;
    private String ageRating;
    private List<String> genres;
    private String stillCutUrl;
    private String posterUrl;
}
