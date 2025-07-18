package com.hamss2.KINO.api.user.controller;

import com.hamss2.KINO.api.user.dto.SimpleUserReqDto;
import com.hamss2.KINO.api.user.service.UserService;
import com.hamss2.KINO.common.exception.BadRequestException;
import com.hamss2.KINO.common.reponse.ApiResponse;
import com.hamss2.KINO.common.reponse.SuccessStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse<SimpleUserReqDto>> getSimpleUserInfo(
        @AuthenticationPrincipal String userId
    ) {
        if (userId == null || userId.isEmpty()) {
            throw new BadRequestException("userId is empty");
        }
        Long id = Long.parseLong(userId);

        return ApiResponse.success(SuccessStatus.SEND_MY_INFO_SUCCESS,
            userService.getSimpleUserInfo(id));
    }
}
