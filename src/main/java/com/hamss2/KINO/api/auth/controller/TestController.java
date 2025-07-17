package com.hamss2.KINO.api.auth.controller;

import com.hamss2.KINO.common.reponse.ApiResponse;
import com.hamss2.KINO.common.reponse.SuccessStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/api")
@RequiredArgsConstructor
public class TestController {

    @GetMapping("/test")
    public ResponseEntity<ApiResponse<String>> getLoginPage() {
        return ApiResponse.success(SuccessStatus.REDIRECT_OAUTH_PAGE_SUCCESS, "성공적으로");
    }
}
