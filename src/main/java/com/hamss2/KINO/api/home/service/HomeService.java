package com.hamss2.KINO.api.home.service;

import com.hamss2.KINO.api.entity.DailyMovieView;
import com.hamss2.KINO.api.entity.Movie;
import com.hamss2.KINO.api.entity.Review;
import com.hamss2.KINO.api.home.dto.res.HomeResponseDto;
import com.hamss2.KINO.api.home.dto.res.MovieDto;
import com.hamss2.KINO.api.home.dto.res.ReviewDto;
import com.hamss2.KINO.api.home.dto.res.TeaserDto;
import com.hamss2.KINO.api.home.repository.DailyMovieViewRepository;
import com.hamss2.KINO.api.home.repository.MyPickMovieRepository;
import com.hamss2.KINO.api.home.repository.ReviewRepository;
import com.hamss2.KINO.api.movieAdmin.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HomeService {

    private final MovieRepository movieRepository;
    private final ReviewRepository reviewRepository;
    private final MyPickMovieRepository myPickMovieRepository;
    private final BoxOfficeService boxOfficeService;
    private final DailyMovieViewRepository dailymovieViewRepository;
    private final RecommendationService recommendationService;

    @Transactional(readOnly = true)
    public HomeResponseDto getHomeData(Long userId) {

        HomeResponseDto homeResponseDto = new HomeResponseDto();

        // 1. 상단 티저 영화
        Movie teaserMovie = movieRepository.findFirstByTeaserUrlIsNotNullOrderByReleaseDateDesc();
        if (teaserMovie != null) {
            homeResponseDto.setTeaser(toTeaserMovieDto(teaserMovie));
        }

        // 2. 사용자 좋아요 TOP 10 리뷰
        homeResponseDto.setTopLikeReviewList(
                reviewRepository.findTop10ByLikeCount().stream()
                        .map(this::toReviewDto)
                        .toList()
        );

        // 3. 사용자 찜 TOP 10 영화
        List<Object[]> topPickMovies = myPickMovieRepository.findTop10MoviesByPickCount();
        List<MovieDto> topMovieList = topPickMovies.stream()
                .map(objArr -> {
                    Movie movie = (Movie) objArr[0];
                    Long pickCount = (Long) objArr[1];
                    MovieDto movieDto = toMovieDto(movie);
                    movieDto.setPickCount(pickCount);
                    return movieDto;
                })
                .collect(Collectors.toList());
        homeResponseDto.setTopPickMovieList(topMovieList);

        // 4. 박스 오피스 TOP 10
        homeResponseDto.setBoxOfficeMovieList(boxOfficeService.fetchRealBoxOfficeTop10());

        // 5. 일별 조회수 TOP 10 영화
        List<DailyMovieView> topViews = dailymovieViewRepository.findTop10ByViewDateOrderByViewDesc(LocalDate.now());
        List<MovieDto> dailyTopMovieList = topViews.stream()
                .map(dmv -> {
                    Movie movie = dmv.getMovie();
                    MovieDto dto = new MovieDto();
                    dto.setMovieId(movie.getMovieId());
                    dto.setTitle(movie.getTitle());
                    dto.setPosterUrl(movie.getPosterUrl());
                    return dto;
                })
                .toList();
        homeResponseDto.setDailyTopMovieList(dailyTopMovieList);

        // 6. 월별 조회수 TOP 10 영화
        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate endOfMonth = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());
        List<Movie> monthlyTopMovies = dailymovieViewRepository.findTop10MovieByMonthView(startOfMonth, endOfMonth);
        List<MovieDto> movies = monthlyTopMovies.stream()
                .map(this::toMovieDto)
                .toList();
        homeResponseDto.setMonthlyTopMovieList(movies);

        // 7. 사용자 기반 추천 TOP 10 영화
        homeResponseDto.setRecommendedMovieList(recommendationService.getRecommendations(userId, 10));

        return homeResponseDto;
    }

    private TeaserDto toTeaserMovieDto(Movie movie) {
        TeaserDto teaserDto = new TeaserDto();
        teaserDto.setMovieId(movie.getMovieId());
        teaserDto.setTitle(movie.getTitle());
        teaserDto.setTeaserUrl(movie.getTeaserUrl());
        return teaserDto;
    }

    private ReviewDto toReviewDto(Review review) {
        ReviewDto reviewDto = new ReviewDto();
        reviewDto.setReviewId(review.getReviewId());
        reviewDto.setReviewTitle(review.getTitle());
        reviewDto.setContent(review.getContent());
        reviewDto.setMovieId(review.getMovie().getMovieId());
        reviewDto.setMovieTitle(review.getMovie().getTitle());
        return reviewDto;
    }

    private MovieDto toMovieDto(Movie movie) {
        MovieDto movieDto = new MovieDto();
        movieDto.setMovieId(movie.getMovieId());
        movieDto.setTitle(movie.getTitle());
        movieDto.setPosterUrl(movie.getPosterUrl());
        return movieDto;
    }
}
