package com.hamss2.KINO.api.movieAdmin.service;


import com.hamss2.KINO.api.entity.Genre;
import com.hamss2.KINO.api.entity.Movie;
import com.hamss2.KINO.api.entity.MovieGenre;
import com.hamss2.KINO.api.movieAdmin.repository.GenreRepository;
import com.hamss2.KINO.api.movieAdmin.repository.MovieGenreRepository;
import com.hamss2.KINO.api.movieAdmin.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;


import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MovieService {

    @Qualifier("tmdbWebClient")
    private final WebClient tmdbWebClient;

    private final MovieRepository movieRepository;
    private final GenreRepository genreRepository;
    private final MovieGenreRepository movieGenreRepository;

    @Value("${tmdb.api-key}")
    private String apiKey;

    @Transactional
    public void fetchAndSaveMovies(int startPage, int endPage) {
        for (int page = startPage; page <= endPage; page++) {
            System.out.println("== TMDB 영화 상세 수집: " + page + " 페이지 ==");
            final int currentPage = page;
            Map<String, Object> result = tmdbWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/discover/movie")
                            .queryParam("language", "ko")
                            .queryParam("api_key", apiKey)
                            .queryParam("sort_by", "popularity.desc")
                            .queryParam("page", currentPage)
                            .build())
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            List<Map<String, Object>> movies = (List<Map<String, Object>>) result.get("results");
            if (movies == null || movies.isEmpty()) break;

            for (Map<String, Object> movie : movies) {
                Long movieId = ((Number) movie.get("id")).longValue();
                if (movieRepository.existsById(movieId)) continue;

                // 1. 영화 상세정보
                Map<String, Object> detail = tmdbWebClient.get()
                        .uri("/movie/{id}?language=ko&api_key={key}", movieId, apiKey)
                        .retrieve().bodyToMono(Map.class).block();

                String title = (String) detail.get("title");
                String overview = (String) detail.get("overview");
                String posterPath = (String) detail.get("poster_path");
                String releaseDate = (String) detail.get("release_date");

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

                // 4. 배우/감독(credits)
                Map<String, Object> credits = tmdbWebClient.get()
                        .uri("/movie/{id}/credits?api_key={key}", movieId, apiKey)
                        .retrieve().bodyToMono(Map.class).block();

                String actors = "";
                String directors = "";
                if (credits != null) {
                    List<Map<String, Object>> cast = (List<Map<String, Object>>) credits.get("cast");
                    if (cast != null) {
                        actors = cast.stream()
                                .limit(5)
                                .map(c -> (String) c.get("name"))
                                .collect(Collectors.joining(", "));
                    }
                    List<Map<String, Object>> crew = (List<Map<String, Object>>) credits.get("crew");
                    if (crew != null) {
                        directors = crew.stream()
                                .filter(c -> "Director".equals(c.get("job")))
                                .map(c -> (String) c.get("name"))
                                .collect(Collectors.joining(", "));
                    }
                }

                // 5. 저장
                Movie entity = new Movie();
                entity.setMovieId(movieId);
                entity.setTitle(title);
                entity.setPlot(overview);
                entity.setPosterUrl(posterPath != null ? "https://image.tmdb.org/t/p/w500" + posterPath : null);
                entity.setTeaserUrl(teaserUrl);
                entity.setActors(actors);
                entity.setDirector(directors);
                entity.setRunningTime(runningTime);
                entity.setReleaseDate((releaseDate != null && !releaseDate.isEmpty()) ? LocalDate.parse(releaseDate) : null);

                movieRepository.save(entity);

                // 5. 장르 매핑 (genre_ids: [28, 12, ...])
                List<Integer> genreIds = (List<Integer>) movie.get("genre_ids");
                if (genreIds != null) {
                    for (Integer gid : genreIds) {
                        Genre genre = genreRepository.findById(Long.valueOf(gid)).orElse(null);
                        if (genre != null) {
                            movieGenreRepository.save(new MovieGenre(null, null, entity, genre));
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
}
