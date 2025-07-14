package com.hamss2.KINO.api.home.dto.res;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class HomeResponseDto {
    private TeaserDto teaser; // 상단 티저
    private List<ReviewDto> topLikeReviewList; // 사용자 좋아요 TOP 10 리뷰
    private List<MovieDto> topPickMovieList;   // 사용자 찜 TOP 10 영화
    private List<MovieDto> boxOfficeMovieList; // 박스 오피스 TOP 10
    private List<MovieDto> dailyTopMovieList;  // 일별 조회수 TOP 10
    private List<MovieDto> monthlyTopMovieList;// 월별 조회수 TOP 10
    private List<MovieDto> recommendedMovieList; // 사용자 기반 추천 TOP 10
}
