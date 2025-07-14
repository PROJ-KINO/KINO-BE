package com.hamss2.KINO.api.home.dto.res;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MovieDto {
    private Long movieId;
    private String title;
    private String posterUrl;
    private Long pickCount;
}
