package com.hamss2.KINO.api.home.dto.req;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GenreSelectReq {
    private List<Long> genreIds;
}
