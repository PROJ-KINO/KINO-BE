package com.hamss2.KINO.api.mypage.controller;

import com.hamss2.KINO.api.entity.User;
import com.hamss2.KINO.api.mypage.dto.*;
import com.hamss2.KINO.api.mypage.service.MypageService;
import com.hamss2.KINO.api.testPackage.UserRepository;
import com.hamss2.KINO.common.reponse.ApiResponse;
import com.hamss2.KINO.common.reponse.SuccessStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mypage")
@Slf4j
public class MypageController {
    private final UserRepository userRepository;
    private final MypageService mypageService;

    @GetMapping("/main")
    public ResponseEntity<ApiResponse<MypageMainResDto>> mypage(@AuthenticationPrincipal UserDetails userDetails) {
//        Long userId = userDetails.getUserId();
        Long userId = 1L;
        MypageMainResDto mainResDto = mypageService.mypage(userId);

        return ApiResponse.success(SuccessStatus.SEARCH_MYPAGE_MAIN_SUCCESS, mainResDto);
    }

    @GetMapping("/shortReview")
    public ResponseEntity<ApiResponse<MypageShortReviewResDto>> shortReview(@AuthenticationPrincipal UserDetails userDetails) {
//        Long userId = userDetails.getUserId();
        Long userId = 1L;
        MypageShortReviewResDto shortReviewResDto = mypageService.shortReview(userId);

        return ApiResponse.success(SuccessStatus.SEARCH_MYPAGE_SHORTREVIEW_SUCCESS, shortReviewResDto);
    }

    @GetMapping("/review")
    public ResponseEntity<ApiResponse<MypageReviewResDto>> review(@AuthenticationPrincipal UserDetails userDetails) {
//        Long userId = userDetails.getUserId();
        Long userId = 1L;
        MypageReviewResDto reviewResDto = mypageService.review(userId);

        return ApiResponse.success(SuccessStatus.SEARCH_MYPAGE_REVIEW_SUCCESS, reviewResDto);
    }

    @GetMapping("/myPickMovie")
    public ResponseEntity<ApiResponse<MypagePickMovieResDto>> myPickMovie(@AuthenticationPrincipal UserDetails userDetails) {
//        Long userId = userDetails.getUserId();
        Long userId = 1L;
        MypagePickMovieResDto pickMovieResDto = mypageService.myPickMovie(userId);

        return ApiResponse.success(SuccessStatus.SEARCH_MYPAGE_PICKMOVIE_SUCCESS, pickMovieResDto);
    }

    @GetMapping("/userGenres")
    public ResponseEntity<ApiResponse<MypageGenreResDto>> genre(@AuthenticationPrincipal UserDetails userDetails) {
//        Long userId = userDetails.getUserId();
        System.out.println(userRepository.findAll());
        Long userId = 1L;
        MypageGenreResDto genreResDto = mypageService.genre(userId);

        return ApiResponse.success(SuccessStatus.SEARCH_MYPAGE_GENRE_SUCCESS, genreResDto);
    }

    @PostMapping("/userGenres")
    public ResponseEntity<ApiResponse<Void>> userGenres(@AuthenticationPrincipal UserDetails userDetails, @RequestBody MypageGenreReqDto genreReqDto) {
//        Long userId = userDetails.getUserId();
        Long userId = 1L;
        
        // 디버깅 로그 추가
        log.info("받은 요청 데이터: {}", genreReqDto);
        log.info("genreIds: {}", genreReqDto != null ? genreReqDto.getGenreIds() : "DTO가 null");
        
        mypageService.updateGenre(userId, genreReqDto);
        return ApiResponse.success_only(SuccessStatus.UPDATE_USERGENRE_SUCCESS);
    }

    @PostMapping(value = "/profile", consumes = "multipart/form-data")
    @Transactional
    public ResponseEntity<ApiResponse<Void>> profile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestPart(value = "nickname", required = false) String nickname,
            @RequestPart(value = "file", required = false) MultipartFile file) {
//        User user = userRepository.findById(userDetails.getUserId());
        User user = userRepository.findById(1L).orElseThrow(() -> new RuntimeException("User not found"));
        
        MypageUpdateProfileReqDto profileReqDto = new MypageUpdateProfileReqDto(nickname, file);
        mypageService.updateProfile(user, profileReqDto);
        return ApiResponse.success_only(SuccessStatus.UPDATE_PROFILE_SUCCESS);
    }
}
