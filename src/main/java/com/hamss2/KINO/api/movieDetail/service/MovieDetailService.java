package com.hamss2.KINO.api.movieDetail.service;

import com.hamss2.KINO.api.entity.DailyMovieView;
import com.hamss2.KINO.api.entity.Movie;
import com.hamss2.KINO.api.entity.MyPickMovie;
import com.hamss2.KINO.api.entity.User;
import com.hamss2.KINO.api.home.repository.DailyMovieViewRepository;
import com.hamss2.KINO.api.home.repository.MyPickMovieRepository;
import com.hamss2.KINO.api.movieAdmin.repository.MovieRepository;
import com.hamss2.KINO.api.movieDetail.dto.res.MovieDetailDto;
import com.hamss2.KINO.api.testPackage.UserRepository;
import com.hamss2.KINO.common.exception.BadRequestException;
import com.hamss2.KINO.common.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MovieDetailService {

    private final UserRepository userRepository;
    private final MovieRepository movieRepository;
    private final MyPickMovieRepository myPickMovieRepository;
    private final DailyMovieViewRepository dailyMovieViewRepository;


    // 찜 등록
    @Transactional
    public void addMyPick(Long movieId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 유저입니다."));
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 영화입니다."));
        if (myPickMovieRepository.findByUserAndMovie(user, movie).isPresent()) {
            throw new BadRequestException("이미 찜한 영화입니다.");
        }
        myPickMovieRepository.save(new MyPickMovie(null, user, movie));
    }

    // 찜 해제
    @Transactional
    public void removeMyPick(Long movieId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 유저입니다."));
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 영화입니다."));
        myPickMovieRepository.deleteByUserAndMovie(user, movie);
    }

    // 찜 여부 확인
    @Transactional(readOnly = true)
    public boolean isMyPick(Long movieId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 유저입니다."));
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 영화입니다."));
        return myPickMovieRepository.findByUserAndMovie(user, movie).isPresent();
    }

    // 작품 정보
    @Transactional(readOnly = true)
    public MovieDetailDto getMovieDetail(Long movieId) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 영화입니다."));

        // 조회수 증가는 별도 트랜잭션으로 비동기 처리
        try {
            incrementViewCount(movieId);
        } catch (Exception e) {
            // 조회수 증가 실패해도 영화 정보는 정상 반환
            System.err.println("조회수 증가 실패: " + e.getMessage());
        }

        return MovieDetailDto.builder()
                .movieId(movie.getMovieId())
                .title(movie.getTitle())
                .plot(movie.getPlot())
                .backdropUrl(movie.getStillCutUrl())
                .releaseDate(movie.getReleaseDate())
                .runningTime(movie.getRunningTime())
                .director(movie.getDirector())
                .ageRating(movie.getAgeRating())
                .genres(movie.getMovieGenres().stream()
                        .map(mg -> mg.getGenre().getGenreName()).distinct().collect(Collectors.toList()))
                .actors(movie.getActors().stream()
                        .map(a -> MovieDetailDto.ActorDto.builder()
                                .name(a.getActor().getName())
                                .profileUrl(a.getActor().getProfileUrl())
                                .build())
                        .collect(Collectors.toList()))
                .otts(movie.getOtts().stream()
                        .map(mo -> MovieDetailDto.OttDto.builder()
                                .name(mo.getOtt().getName())
                                .logoUrl(mo.getOtt().getLogoUrl())
                                .linkUrl(mo.getLinkUrl())
                                .build())
                        .collect(Collectors.toList()))
                .teaserUrl(movie.getTeaserUrl())
                .avgRating(movie.getAvgRating())
                .build();
    }

    // 조회수 증가 (별도 트랜잭션으로 비동기 처리)
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void incrementViewCount(Long movieId) {
        try {
            Movie movie = movieRepository.findById(movieId)
                    .orElseThrow(() -> new NotFoundException("존재하지 않는 영화입니다."));

            // 누적 조회수 증가
            movie.setTotalView(movie.getTotalView() + 1);

            // 일간 조회수 증가
            LocalDate today = LocalDate.now();
            DailyMovieView todayView = dailyMovieViewRepository
                    .findByMovieAndViewDate(movie, today)
                    .orElseGet(() -> {
                        DailyMovieView newView = new DailyMovieView();
                        newView.setMovie(movie);
                        newView.setViewDate(today);
                        newView.setDailyView(0);
                        return newView;
                    });
            todayView.setDailyView(todayView.getDailyView() + 1);
            dailyMovieViewRepository.save(todayView);
            
        } catch (Exception e) {
            // 조회수 증가 실패 로그 (서비스에 영향 없음)
            System.err.println("조회수 증가 중 오류 발생 - 영화ID: " + movieId + ", 오류: " + e.getMessage());
        }
    }

}
