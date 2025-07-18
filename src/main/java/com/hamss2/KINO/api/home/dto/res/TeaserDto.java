package com.hamss2.KINO.api.home.dto.res;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TeaserDto {
    private Long movieId;
    private String title;
    private String teaserUrl;
    private String plot;
    private String stillCutUrl;
}
