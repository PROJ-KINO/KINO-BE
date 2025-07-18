package com.hamss2.KINO.api.mypage.controller;

import com.hamss2.KINO.api.entity.Follow;
import com.hamss2.KINO.api.entity.User;
import com.hamss2.KINO.api.mypage.dto.*;
import com.hamss2.KINO.api.follow.repository.FollowRepository;
import com.hamss2.KINO.api.mypage.service.MypageService;
import com.hamss2.KINO.api.testPackage.UserRepository;
import com.hamss2.KINO.common.reponse.ApiResponse;
import com.hamss2.KINO.common.reponse.SuccessStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mypage")
@Slf4j
public class MypageController {
    private final UserRepository userRepository;
    private final MypageService mypageService;
    private final FollowRepository followRepository;

    @GetMapping("/main")
    public ResponseEntity<ApiResponse<MypageMainResDto>> mypage(@AuthenticationPrincipal String userId) {
        MypageMainResDto mainResDto = mypageService.mypage(Long.valueOf(userId));

        return ApiResponse.success(SuccessStatus.SEARCH_MYPAGE_MAIN_SUCCESS, mainResDto);
    }

    @GetMapping("/shortReview")
    public ResponseEntity<ApiResponse<MypageShortReviewResDto>> shortReview(@AuthenticationPrincipal String userId) {
        MypageShortReviewResDto shortReviewResDto = mypageService.shortReview(Long.valueOf(userId));

        return ApiResponse.success(SuccessStatus.SEARCH_MYPAGE_SHORTREVIEW_SUCCESS, shortReviewResDto);
    }

    @GetMapping("/review")
    public ResponseEntity<ApiResponse<MypageReviewResDto>> review(@AuthenticationPrincipal String userId) {
        MypageReviewResDto reviewResDto = mypageService.review(Long.valueOf(userId));

        return ApiResponse.success(SuccessStatus.SEARCH_MYPAGE_REVIEW_SUCCESS, reviewResDto);
    }

    @GetMapping("/myPickMovie")
    public ResponseEntity<ApiResponse<MypagePickMovieResDto>> myPickMovie(@AuthenticationPrincipal String userId) {
        MypagePickMovieResDto pickMovieResDto = mypageService.myPickMovie(Long.valueOf(userId));

        return ApiResponse.success(SuccessStatus.SEARCH_MYPAGE_PICKMOVIE_SUCCESS, pickMovieResDto);
    }

    @GetMapping("/userGenres")
    public ResponseEntity<ApiResponse<MypageGenreResDto>> genre(@AuthenticationPrincipal String userId) {
        MypageGenreResDto genreResDto = mypageService.genre(Long.valueOf(userId));

        return ApiResponse.success(SuccessStatus.SEARCH_MYPAGE_GENRE_SUCCESS, genreResDto);
    }

    @PostMapping("/userGenres")
    public ResponseEntity<ApiResponse<Void>> userGenres(@AuthenticationPrincipal String userId, @RequestBody MypageGenreReqDto genreReqDto) {
        // 디버깅 로그 추가
        log.info("받은 요청 데이터: {}", genreReqDto);
        log.info("genreIds: {}", genreReqDto != null ? genreReqDto.getGenreIds() : "DTO가 null");
        
        mypageService.updateGenre(Long.valueOf(userId), genreReqDto);
        return ApiResponse.success_only(SuccessStatus.UPDATE_USERGENRE_SUCCESS);
    }

    @PostMapping(value = "/profile", consumes = "multipart/form-data")
    @Transactional
    public ResponseEntity<ApiResponse<Void>> profile(
            @AuthenticationPrincipal String userId,
            @RequestPart(value = "nickname", required = false) String nickname,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        User user = userRepository.findById(Long.valueOf(userId)).orElseThrow(() -> new RuntimeException("User not found"));
        
        MypageUpdateProfileReqDto profileReqDto = new MypageUpdateProfileReqDto(nickname, file);
        mypageService.updateProfile(user, profileReqDto);
        return ApiResponse.success_only(SuccessStatus.UPDATE_PROFILE_SUCCESS);
    }
}
