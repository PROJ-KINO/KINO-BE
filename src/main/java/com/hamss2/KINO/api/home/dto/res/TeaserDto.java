package com.hamss2.KINO.api.home.dto.res;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class TeaserDto {
    private Long movieId;
    private String title;
    private String teaserUrl;
    private String plot;
    private String stillCutUrl;
    private LocalDate releaseDate;
    private int runningTime;
    private List<String> genres;
}
