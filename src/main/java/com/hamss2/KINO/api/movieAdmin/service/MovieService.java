package com.hamss2.KINO.api.movieAdmin.service;


import com.hamss2.KINO.api.entity.*;
import com.hamss2.KINO.api.movieAdmin.repository.*;
import com.hamss2.KINO.api.searchMovie.dto.MovieResDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;


import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class MovieService {

    @Qualifier("tmdbWebClient")
    private final WebClient tmdbWebClient;

    private final MovieRepository movieRepository;
    private final GenreRepository genreRepository;
    private final MovieGenreRepository movieGenreRepository;
    private final ActorRepository actorRepository;
    private final MovieActorRepository movieActorRepository;
    private final OttRepository ottRepository;
    private final MovieOttRepository movieOttRepository;

    @Value("${tmdb.api-key}")
    private String apiKey;

    public void fetchAndSaveMovies(int startPage, int endPage, String sortBy) {
        for (int page = startPage; page <= endPage; page++) {
            System.out.println("== TMDB 영화 상세 수집: " + page + " 페이지 ==");
            final int currentPage = page;
            Map<String, Object> result = tmdbWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/discover/movie")
                            .queryParam("language", "ko")
                            .queryParam("api_key", apiKey)
                            .queryParam("sort_by", sortBy)
                            .queryParam("page", currentPage)
                            .build())
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            List<Map<String, Object>> movies = (List<Map<String, Object>>) result.get("results");
            if (movies == null || movies.isEmpty()) break;

            for (Map<String, Object> movie : movies) {
                Long movieId = ((Number) movie.get("id")).longValue();

                // 1. 영화 상세정보
                Map<String, Object> detail = tmdbWebClient.get()
                        .uri("/movie/{id}?language=ko&api_key={key}", movieId, apiKey)
                        .retrieve().bodyToMono(Map.class).block();

                String title = (String) detail.get("title");
                String overview = (String) detail.get("overview");
                String posterPath = (String) detail.get("poster_path");
                String releaseDate = (String) detail.get("release_date");

                // releaseDate가 null이거나 빈 값이면, 저장하지 않고 건너뜀
                if (releaseDate == null || releaseDate.isEmpty()) {
                    System.out.println("release_date 없는 영화 SKIP: " + title + " (" + movieId + ")");
                    continue;
                }

                // == 평점 ==
                String avgRating = null;
                if (detail.get("vote_average") != null) {
                    Double rating = ((Number) detail.get("vote_average")).doubleValue();
                    avgRating = String.format("%.1f", rating);
                }

                // 2. 러닝타임
                Integer runningTime = null;
                if (detail.get("runtime") != null) {
                    runningTime = ((Number) detail.get("runtime")).intValue();
                }

                // 3. 예고편/티저 영상
                Map<String, Object> videos = tmdbWebClient.get()
                        .uri("/movie/{id}/videos?language=ko&api_key={key}", movieId, apiKey)
                        .retrieve().bodyToMono(Map.class).block();

                String teaserUrl = null;
                if (videos != null && videos.containsKey("results")) {
                    List<Map<String, Object>> videoList = (List<Map<String, Object>>) videos.get("results");
                    Optional<Map<String, Object>> teaser = videoList.stream()
                            .filter(v -> "Teaser".equalsIgnoreCase((String) v.get("type")) && "YouTube".equals(v.get("site")))
                            .findFirst();
                    if (!teaser.isPresent()) {
                        teaser = videoList.stream()
                                .filter(v -> "Trailer".equalsIgnoreCase((String) v.get("type")) && "YouTube".equals(v.get("site")))
                                .findFirst();
                    }
                    if (teaser.isPresent()) {
                        teaserUrl = "https://www.youtube.com/watch?v=" + teaser.get().get("key");
                    }
                }

                // 4. 스틸컷
                Map<String, Object> images = tmdbWebClient.get()
                        .uri("/movie/{id}/images?api_key={key}", movieId, apiKey)
                        .retrieve().bodyToMono(Map.class).block();
                String stillCutUrl = null;
                if (images != null && images.containsKey("backdrops")) {
                    List<Map<String, Object>> backdrops = (List<Map<String, Object>>) images.get("backdrops");
                    if (backdrops != null && !backdrops.isEmpty()) {
                        stillCutUrl = "https://image.tmdb.org/t/p/w780" + backdrops.get(0).get("file_path");
                    }
                }

                // 5. 연령등급(관람등급)
                Map<String, Object> releaseDates = tmdbWebClient.get()
                        .uri("/movie/{id}/release_dates?api_key={key}", movieId, apiKey)
                        .retrieve().bodyToMono(Map.class).block();
                String ageRating = null;
                if (releaseDates != null && releaseDates.containsKey("results")) {
                    List<Map<String, Object>> results = (List<Map<String, Object>>) releaseDates.get("results");
                    Optional<Map<String, Object>> kr = results.stream()
                            .filter(r -> "KR".equals(r.get("iso_3166_1")))
                            .findFirst();
                    if (kr.isPresent()) {
                        List<Map<String, Object>> rels = (List<Map<String, Object>>) kr.get().get("release_dates");
                        Optional<String> cert = rels.stream()
                                .map(r -> (String) r.get("certification"))
                                .filter(c -> c != null && !c.isEmpty())
                                .findFirst();
                        if (cert.isPresent()) ageRating = cert.get();
                    }
                }

                // 6. 감독(credits)
                Map<String, Object> credits = tmdbWebClient.get()
                        .uri("/movie/{id}/credits?api_key={key}", movieId, apiKey)
                        .retrieve().bodyToMono(Map.class).block();

                String directors = "";
                if (credits != null) {
                    List<Map<String, Object>> crew = (List<Map<String, Object>>) credits.get("crew");
                    if (crew != null) {
                        directors = crew.stream()
                                .filter(c -> "Director".equals(c.get("job")))
                                .map(c -> (String) c.get("name"))
                                .collect(Collectors.joining(", "));
                    }
                }

                // 7. 영화 존재하면 update, 없으면 insert
                Movie entity = movieRepository.findById(movieId).orElseGet(Movie::new);
                entity.setMovieId(movieId);
                entity.setTitle(title);
                entity.setPlot(overview);
                entity.setAvgRating(avgRating);
                entity.setPosterUrl(posterPath != null ? "https://image.tmdb.org/t/p/w500" + posterPath : null);
                entity.setTeaserUrl(teaserUrl);
                entity.setDirector(directors);
                entity.setRunningTime(runningTime);
                entity.setReleaseDate((releaseDate != null && !releaseDate.isEmpty()) ? LocalDate.parse(releaseDate) : null);
                entity.setStillCutUrl(stillCutUrl);
                entity.setAgeRating(ageRating);

                movieRepository.save(entity);

                // 8. 장르 매핑 (genre_ids: [28, 12, ...])
                List<Integer> genreIds = (List<Integer>) movie.get("genre_ids");
                if (genreIds != null) {
                    for (Integer gid : genreIds) {
                        Genre genre = genreRepository.findById(Long.valueOf(gid)).orElse(null);
                        if (genre != null) {
                            movieGenreRepository.save(new MovieGenre(null, null, entity, genre));
                        }
                    }
                }

                // 9. 배우 저장 (최대 5명)
                if (credits != null) {
                    List<Map<String, Object>> cast = (List<Map<String, Object>>) credits.get("cast");
                    if (cast != null) {
                        cast.stream().limit(5).forEach(actorMap -> {
                            Long actorId = ((Number) actorMap.get("id")).longValue();
                            String actorName = (String) actorMap.get("name");
                            String profilePath = (String) actorMap.get("profile_path");
                            String profileUrl = profilePath != null ? "https://image.tmdb.org/t/p/w185" + profilePath : null;

                            // 배우가 이미 저장돼있는지 확인
                            Actor actor = actorRepository.findById(actorId).orElse(null);
                            if (actor == null) {
                                actor = new Actor();
                                actor.setActorId(actorId);
                                actor.setName(actorName);
                                actor.setProfileUrl(profileUrl);
                                actorRepository.save(actor);
                            }
                            // MovieActor 관계 저장
                            MovieActor movieActor = new MovieActor();
                            movieActor.setMovie(entity);
                            movieActor.setActor(actor);
                            movieActorRepository.save(movieActor);
                        });
                    }
                }

                // 10. OTT 정보 저장
                Map<String, Object> providerData = tmdbWebClient.get()
                        .uri("/movie/{id}/watch/providers?api_key={key}", movieId, apiKey)
                        .retrieve().bodyToMono(Map.class).block();

                if (providerData != null && providerData.containsKey("results")) {
                    Map<String, Object> results = (Map<String, Object>) providerData.get("results");
                    if (results.containsKey("KR")) { // "KR"은 한국 기준, 필요시 국가 코드 변경
                        Map<String, Object> kr = (Map<String, Object>) results.get("KR");
                        String linkUrl = (String) kr.get("link");
                        List<Map<String, Object>> flatrate = (List<Map<String, Object>>) kr.get("flatrate");
                        if (flatrate != null) {
                            for (Map<String, Object> ottMap : flatrate) {
                                Long ottId = ((Number) ottMap.get("provider_id")).longValue();
                                String ottName = (String) ottMap.get("provider_name");
                                String ottLogo = ottMap.get("logo_path") != null ?
                                        "https://image.tmdb.org/t/p/w92" + ottMap.get("logo_path") : null;

                                // 1) Ott 엔티티 저장(중복 방지)
                                Ott ott = ottRepository.findById(ottId).orElse(null);
                                if (ott == null) {
                                    ott = new Ott();
                                    ott.setOttId(ottId);
                                    ott.setName(ottName);
                                    ott.setLogoUrl(ottLogo);
                                    ottRepository.save(ott);
                                }

                                // 2) MovieOtt 연결 저장 (동일 OTT+영화 중복 방지)
                                if (!movieOttRepository.existsByMovieAndOtt(entity, ott)) {
                                    MovieOtt movieOtt = new MovieOtt();
                                    movieOtt.setMovie(entity);
                                    movieOtt.setOtt(ott);
                                    movieOtt.setLinkUrl(linkUrl);
                                    movieOttRepository.save(movieOtt);
                                }
                            }
                        }
                    }
                }
            }
        }
        System.out.println("=== TMDB 영화 상세 전체 수집 완료 ===");
    }

    public void fetchAndSaveGenres() {
        Map<String, Object> result = tmdbWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/genre/movie/list")
                        .queryParam("language", "ko")
                        .queryParam("api_key", apiKey)
                        .build())
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        List<Map<String, Object>> genres = (List<Map<String, Object>>) result.get("genres");
        if (genres == null) return;

        for (Map<String, Object> genre : genres) {
            Long id = ((Number) genre.get("id")).longValue();
            String name = (String) genre.get("name");
            if (!genreRepository.existsById(id)) {
                Genre genreEntity = new Genre();
                genreEntity.setGenreId(id);
                genreEntity.setGenreName(name);
                genreRepository.save(genreEntity);
            }
        }
    }

    public List<MovieResDto> allMovie(){
        return movieRepository.findAll().stream()
                .map(m -> new MovieResDto(
                        m.getTitle(),
                        m.getMovieId(),
                        m.getPosterUrl(),
                        movieGenreRepository.findByMovie_MovieId(m.getMovieId()).stream()
                                .map(mG -> mG.getGenre().getGenreId()).toList()
                )).toList();
    }

    public Page<MovieResDto> allMovies(Pageable pageable, List<Long> ids){
        Page<Movie> movies = movieRepository.findByMovieIdIn(ids, pageable); // ✨ 페이징 + ids 필터링

        List<MovieResDto> dtoList = movies.stream()
                .map(m -> new MovieResDto(
                        m.getTitle(),
                        m.getMovieId(),
                        m.getPosterUrl(),
                        movieGenreRepository.findByMovie_MovieId(m.getMovieId()).stream()
                                .map(mG -> mG.getGenre().getGenreName()).toList()
                )).toList();

        return new PageImpl<>(dtoList, pageable, movies.getTotalElements());
    }
}
