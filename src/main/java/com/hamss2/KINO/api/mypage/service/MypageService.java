package com.hamss2.KINO.api.mypage.service;

import com.hamss2.KINO.api.entity.User;
import com.hamss2.KINO.api.entity.UserGenre;
import com.hamss2.KINO.api.entity.Genre;
import com.hamss2.KINO.api.entity.ShortReview;
import com.hamss2.KINO.api.entity.Review;
import com.hamss2.KINO.api.image.GcsUploader;
import com.hamss2.KINO.api.mypage.dto.MypageGenreReqDto;
import com.hamss2.KINO.api.mypage.dto.MypageGenreResDto;
import com.hamss2.KINO.api.mypage.dto.MypageMainResDto;
import com.hamss2.KINO.api.mypage.dto.MypageReviewResDto;
import com.hamss2.KINO.api.mypage.dto.MypageShortReviewResDto;
import com.hamss2.KINO.api.mypage.dto.MypagePickMovieResDto;
import com.hamss2.KINO.api.mypage.dto.MypageUpdateProfileReqDto;
import com.hamss2.KINO.api.testPackage.UserRepository;
import com.hamss2.KINO.api.movieAdmin.repository.GenreRepository;
import com.hamss2.KINO.api.mypage.repository.UserGenreRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Comparator;

@Service
@RequiredArgsConstructor
@Transactional
public class MypageService {

    private final UserRepository userRepository;
    private final GenreRepository genreRepository;
    private final UserGenreRepository userGenreRepository;
    private final GcsUploader gcsUploader;

    @Transactional
    public void updateGenre(Long userId, MypageGenreReqDto mypageGenreReqDto) {
        if (mypageGenreReqDto == null) {
            throw new IllegalArgumentException("MypageGenreReqDto cannot be null");
        }
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        userGenreRepository.deleteByUser_UserId(userId);
        
        List<Long> genreIds = mypageGenreReqDto.getGenreIds();
        if (genreIds == null || genreIds.isEmpty()) {
            return;
        }
        
        List<UserGenre> newUserGenres = genreIds.stream()
                .filter(genreId -> genreId != null)
                .map(genreId -> {
                    Genre genre = genreRepository.findById(genreId)
                            .orElseThrow(() -> new RuntimeException("Genre not found: " + genreId));
                    
                    UserGenre userGenre = new UserGenre();
                    userGenre.setUser(user);
                    userGenre.setGenre(genre);
                    return userGenre;
                })
                .collect(Collectors.toList());
        
        userGenreRepository.saveAll(newUserGenres);
    }

    public void updateNickname(User user, MypageUpdateProfileReqDto mypageUpdateProfileReqDto) {
        user.setNickname(mypageUpdateProfileReqDto.getNickname());
        userRepository.save(user);
    }

    public void updateImage(User user, MypageUpdateProfileReqDto mypageUpdateProfileReqDto) {
        // 새 이미지 업로드
        String newImageUrl = gcsUploader.uploadFile(mypageUpdateProfileReqDto.getFile());
        
        // 업로드 성공시에만 기존 이미지 삭제 및 업데이트
        if (newImageUrl != null) {
            // 기존 이미지가 GCS URL 형식인 경우에만 삭제
            String oldImageUrl = user.getImage();
            if (oldImageUrl != null && !oldImageUrl.isEmpty() && isGcsUrl(oldImageUrl)) {
                gcsUploader.deleteFile(oldImageUrl);
            }
            
            user.setImage(newImageUrl);
            userRepository.save(user);
        } else {
            throw new RuntimeException("이미지 업로드에 실패했습니다.");
        }
    }
    
    private boolean isGcsUrl(String url) {
        return url.startsWith("https://storage.googleapis.com/");
    }

    public void updateProfile(User user, MypageUpdateProfileReqDto mypageUpdateProfileReqDto) {
        if(mypageUpdateProfileReqDto.getNickname() != null && !mypageUpdateProfileReqDto.getNickname().trim().isEmpty()) {
            updateNickname(user, mypageUpdateProfileReqDto);
        }
        if(mypageUpdateProfileReqDto.getFile() != null && !mypageUpdateProfileReqDto.getFile().isEmpty()) {
            updateImage(user, mypageUpdateProfileReqDto);
        }
    }

    @Transactional(readOnly = true)
    public MypageMainResDto mypage(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        
        String nickname = user.getNickname();
        String image = user.getImage();
        
        List<ShortReview> shortReviews = user.getShortReviews();
        MypageMainResDto.ShortReviewDto latestShortReviewDto = shortReviews.stream()
                .max(Comparator.comparing(ShortReview::getCreatedAt))
                .map(sr -> new MypageMainResDto.ShortReviewDto(
                        sr.getShortReviewId(),
                        sr.getContent(),
                        sr.getMovie().getTitle(),
                        sr.getCreatedAt()
                ))
                .orElse(null);
        
        List<Review> reviews = user.getReviews();
        MypageMainResDto.ReviewDto latestReviewDto = reviews.stream()
                .max(Comparator.comparing(Review::getCreatedAt))
                .map(r -> new MypageMainResDto.ReviewDto(
                        r.getReviewId(),
                        r.getTitle(),
                        r.getContent(),
                        r.getMovie().getTitle(),
                        r.getCreatedAt()
                ))
                .orElse(null);
        
        int followersCount = user.getFollowers().size();
        int followingCount = user.getFollowing().size();
        
        List<MypageMainResDto.MyPickMovieDto> myPickMovieDtos = user.getMyPickMovies().stream()
                .limit(5)
                .map(mpm -> new MypageMainResDto.MyPickMovieDto(
                        mpm.getMyPickMovieId(),
                        mpm.getMovie().getTitle(),
                        mpm.getMovie().getPosterUrl()
                ))
                .collect(Collectors.toList());
        
        return new MypageMainResDto(nickname, image, latestShortReviewDto, latestReviewDto, 
                                   followersCount, followingCount, myPickMovieDtos);
    }

    @Transactional(readOnly = true)
    public MypageGenreResDto genre(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        
        String nickname = user.getNickname();
        int followersCount = user.getFollowers().size();
        int followingCount = user.getFollowing().size();
        
        List<MypageGenreResDto.UserGenreDto> userGenreDtos = user.getUserGenres().stream()
                .map(userGenre -> new MypageGenreResDto.UserGenreDto(
                        userGenre.getUserGenreId(),
                        userGenre.getGenre().getGenreName()
                ))
                .collect(Collectors.toList());
        
        return new MypageGenreResDto(nickname, followersCount, followingCount, userGenreDtos);
    }

    @Transactional(readOnly = true)
    public MypageReviewResDto review(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        
        List<MypageReviewResDto.ReviewDto> reviewDtos = user.getReviews().stream()
                .map(review -> new MypageReviewResDto.ReviewDto(
                        review.getReviewId(),
                        review.getTitle(),
                        review.getContent(),
                        review.getMovie().getTitle(),
                        review.getTotalViews(),
                        review.getCreatedAt()
                ))
                .collect(Collectors.toList());
        
        return new MypageReviewResDto(reviewDtos);
    }

    @Transactional(readOnly = true)
    public MypageShortReviewResDto shortReview(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        
        List<MypageShortReviewResDto.ShortReviewDto> shortReviewDtos = user.getShortReviews().stream()
                .map(shortReview -> new MypageShortReviewResDto.ShortReviewDto(
                        shortReview.getShortReviewId(),
                        shortReview.getContent(),
                        shortReview.getRating(),
                        shortReview.getMovie().getTitle(),
                        shortReview.getCreatedAt()
                ))
                .collect(Collectors.toList());
        
        return new MypageShortReviewResDto(shortReviewDtos);
    }

    @Transactional(readOnly = true)
    public MypagePickMovieResDto myPickMovie(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        
        List<MypagePickMovieResDto.MyPickMovieDto> myPickMovieDtos = user.getMyPickMovies().stream()
                .map(myPickMovie -> new MypagePickMovieResDto.MyPickMovieDto(
                        myPickMovie.getMyPickMovieId(),
                        myPickMovie.getMovie().getTitle(),
                        myPickMovie.getMovie().getPosterUrl()
                ))
                .collect(Collectors.toList());
        
        return new MypagePickMovieResDto(myPickMovieDtos);
    }
}
