package com.hamss2.KINO.api.movieDetail.service;

import com.hamss2.KINO.api.entity.Movie;
import com.hamss2.KINO.api.entity.MyPickMovie;
import com.hamss2.KINO.api.entity.User;
import com.hamss2.KINO.api.home.repository.MyPickMovieRepository;
import com.hamss2.KINO.api.movieAdmin.repository.MovieRepository;
import com.hamss2.KINO.api.movieDetail.dto.res.MovieDetailDto;
import com.hamss2.KINO.api.testPackage.UserRepository;
import com.hamss2.KINO.common.exception.BadRequestException;
import com.hamss2.KINO.common.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MovieDetailService {

    private final UserRepository userRepository;
    private final MovieRepository movieRepository;
    private final MyPickMovieRepository myPickMovieRepository;


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
    @Transactional
    public MovieDetailDto getMovieDetail(Long movieId) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 영화입니다."));
        movie.setTotalView(movie.getTotalView() + 1);
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
                        .map(mg -> mg.getGenre().getGenreName())
                        .collect(Collectors.toList()))
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


}
