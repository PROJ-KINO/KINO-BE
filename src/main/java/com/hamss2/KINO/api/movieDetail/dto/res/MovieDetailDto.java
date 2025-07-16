package com.hamss2.KINO.api.movieDetail.dto.res;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class MovieDetailDto {

    private Long movieId;
    private String title;
    private String plot;
    private String backdropUrl;
    private LocalDate releaseDate;
    private int runningTime;
    private String ageRating;
    private List<String> genres;
    private String director;
    private List<ActorDto> actors;
    private List<OttDto> otts;
    private String teaserUrl;

    @Getter
    @Builder
    public static class ActorDto {
        private String name;
        private String profileUrl;
    }

    @Getter
    @Builder
    public static class OttDto {
        private String name;
        private String logoUrl;
        private String linkUrl; // 해당 OTT에서 볼 수 있는 영화 상세 페이지 URL
    }


}
