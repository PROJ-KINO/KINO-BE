package com.hamss2.KINO.api.home.controller;

import com.hamss2.KINO.api.home.dto.req.GenreSelectReq;
import com.hamss2.KINO.api.home.dto.res.HomeResponseDto;
import com.hamss2.KINO.api.home.service.HomeService;
import com.hamss2.KINO.common.reponse.ApiResponse;
import com.hamss2.KINO.common.reponse.SuccessStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class HomeController {

    private final HomeService homeService;

    @GetMapping("/home")
    public ResponseEntity<ApiResponse<HomeResponseDto>> getHomeData(Long userId) {
//        Long userId = (userDetail != null) ? userDetail.getUserId() : null;
        HomeResponseDto homeResponseDto = homeService.getHomeData(userId);
        return ApiResponse.success(SuccessStatus.SEND_HOME_SUCCESS, homeResponseDto);
    }

    @PostMapping("/user/genre")
    public ResponseEntity<ApiResponse<Void>> selectGenre(@RequestBody GenreSelectReq req, Long userId) {
        homeService.saveUserGenres(req, userId);
        return ApiResponse.success_only(SuccessStatus.SEND_USER_GENRE_SELECT_SUCCESS);
    }
}
