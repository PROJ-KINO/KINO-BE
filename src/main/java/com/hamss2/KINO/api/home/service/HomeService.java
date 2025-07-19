package com.hamss2.KINO.api.home.service;

import com.hamss2.KINO.api.entity.*;
import com.hamss2.KINO.api.home.dto.req.GenreSelectReq;
import com.hamss2.KINO.api.home.dto.res.HomeResponseDto;
import com.hamss2.KINO.api.home.dto.res.MovieDto;
import com.hamss2.KINO.api.home.dto.res.ReviewDto;
import com.hamss2.KINO.api.home.dto.res.TeaserDto;
import com.hamss2.KINO.api.home.repository.*;
import com.hamss2.KINO.api.image.controller.ImageController;
import com.hamss2.KINO.api.movieAdmin.repository.GenreRepository;
import com.hamss2.KINO.api.movieAdmin.repository.MovieRepository;
import com.hamss2.KINO.api.mypage.repository.UserGenreRepository;
import com.hamss2.KINO.api.testPackage.UserRepository;
import com.hamss2.KINO.common.exception.BadRequestException;
import com.hamss2.KINO.common.exception.InternalServerException;
import com.hamss2.KINO.common.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
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
    private final UserGenreRepository userGenreRepository;
    private final UserRepository userRepository;
    private final GenreRepository genreRepository;

    @Transactional(readOnly = true)
    public HomeResponseDto getHomeData(Long userId) {

        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("존재하지 않는 유저입니다."));

        HomeResponseDto homeResponseDto = new HomeResponseDto();

        // 1. 상단 티저 영화
        Movie teaserMovie = movieRepository.findFirstByTeaserUrlIsNotNullAndPlotIsNotNullAndPlotNotOrderByReleaseDateDesc("");
        if (teaserMovie != null) {
            homeResponseDto.setTeaser(toTeaserMovieDto(teaserMovie));
        }

        // 2. 사용자 좋아요 TOP 10 리뷰
        homeResponseDto.setTopLikeReviewList(
                reviewRepository.findTop10ByReviewLikes(PageRequest.of(0, 10)).stream()
                        .map(this::toReviewDto)
                        .toList()
        );

        // 3. 사용자 찜 TOP 10 영화
        List<Movie> topPickMovies = myPickMovieRepository.findTop10MoviesByPickCount();
        List<MovieDto> topMovieList = topPickMovies.stream()
                .map(movie -> {
                    MovieDto movieDto = toMovieDto(movie);
                    return movieDto;
                })
                .collect(Collectors.toList());
        homeResponseDto.setTopPickMovieList(topMovieList);

        // 4. 박스 오피스 TOP 10
        try {
            homeResponseDto.setBoxOfficeMovieList(boxOfficeService.fetchRealBoxOfficeTop10());
        } catch (Exception e) {
            throw new InternalServerException("박스오피스 서버 오류" + e);
        }

        // 5. 일별 조회수 TOP 10 영화
        List<DailyMovieView> topViews = dailymovieViewRepository.findByViewDateOrderByDailyViewDesc(LocalDate.now(), PageRequest.of(0, 10));
        List<MovieDto> dailyTopMovieList = topViews.stream()
                .map(dmv -> {
                    Movie movie = dmv.getMovie();
                    MovieDto dto = new MovieDto();
                    dto.setMovieId(movie.getMovieId());
                    dto.setTitle(movie.getTitle());
                    dto.setPosterUrl(movie.getStillCutUrl());
                    return dto;
                })
                .toList();
        homeResponseDto.setDailyTopMovieList(dailyTopMovieList);

        // 6. 월별 조회수 TOP 10 영화
        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate endOfMonth = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());
        List<Movie> monthlyTopMovies = dailymovieViewRepository.findTop10MovieByMonthView(startOfMonth, endOfMonth, PageRequest.of(0, 10));
        List<MovieDto> movies = monthlyTopMovies.stream()
                .map(this::toMovieDto)
                .toList();
        homeResponseDto.setMonthlyTopMovieList(movies);

        // 7. 사용자 기반 추천 TOP 10 영화
        try {
            List<MovieDto> recommended = recommendationService.getRecommendations(userId, 10);
            homeResponseDto.setRecommendedMovieList(recommended);
        } catch (Exception e) {
            throw new InternalServerException("플라스크 서버 오류");
        }

        return homeResponseDto;
    }

    @Transactional
    public void saveUserGenres(GenreSelectReq req, Long userId) {

        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("존재하지 않는 유저입니다."));

        if (!user.getIsFirstLogin()) {
            throw new BadRequestException("이미 취향 장르를 선택하셨습니다.");
        }

        List<Genre> genres = genreRepository.findAllById(req.getGenreIds());

        List<UserGenre> userGenres = genres.stream()
                .map(genre -> new UserGenre(null, null, user, genre))
                .toList();

        userGenreRepository.saveAll(userGenres);

        user.setIsFirstLogin(false);
        userRepository.save(user);
    }

    public List<MovieDto> searchMovies(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return List.of();
        }
        List<Movie> movies = movieRepository.findByTitleContaining(keyword);
        return movies.stream().map(this::toMovieDto).toList();
    }

    private TeaserDto toTeaserMovieDto(Movie movie) {
        TeaserDto teaserDto = new TeaserDto();
        teaserDto.setMovieId(movie.getMovieId());
        teaserDto.setTitle(movie.getTitle());

        String originalUrl = movie.getTeaserUrl();
        String videoId = originalUrl.substring(originalUrl.indexOf("v=") + 2);
        String embedUrl = "https://www.youtube.com/embed/" + videoId
                + "?autoplay=1&controls=0&showinfo=0&rel=0" + "&modestbranding=1" + "&loop=1&playlist=" + videoId;

        teaserDto.setTeaserUrl(embedUrl);
        teaserDto.setPlot(movie.getPlot());
        teaserDto.setStillCutUrl(movie.getStillCutUrl());
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
        movieDto.setPosterUrl(movie.getStillCutUrl());
        return movieDto;
    }
}
