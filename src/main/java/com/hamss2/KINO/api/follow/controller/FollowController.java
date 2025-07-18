package com.hamss2.KINO.api.follow.controller;

import com.hamss2.KINO.api.follow.dto.FollowStatusDto;
import com.hamss2.KINO.api.follow.dto.FollowUserDto;
import com.hamss2.KINO.api.follow.service.FollowService;
import com.hamss2.KINO.common.reponse.ApiResponse;
import com.hamss2.KINO.common.reponse.SuccessStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/follow")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    @PostMapping("/{targetUserId}")
    public void follow(@AuthenticationPrincipal String userId,
                       @PathVariable Long targetUserId) {
        followService.follow(Long.valueOf(userId), targetUserId);
    }

    @DeleteMapping("/{targetUserId}")
    public void unfollow(@AuthenticationPrincipal String userId,
                         @PathVariable Long targetUserId) {
        followService.unfollow(Long.valueOf(userId), targetUserId);
    }

    @GetMapping("/followers/{targetId}")
    public ResponseEntity<ApiResponse<List<FollowUserDto>>> getFollowers(@AuthenticationPrincipal String userId, @PathVariable int targetId) {
        return ApiResponse.success(SuccessStatus.SEARCH_ALL_FOLLOWER_SUCCESS, followService.getFollowers(Long.valueOf(userId), (long) targetId));
    }

    @GetMapping("/following/{targetId}")
    public ResponseEntity<ApiResponse<List<FollowUserDto>>> getFollowings(@AuthenticationPrincipal String userId, @PathVariable int targetId) {
        return ApiResponse.success(SuccessStatus.SEARCH_ALL_FOLLOWING_SUCCESS, followService.getFollowings(Long.valueOf(userId), (long) targetId));
    }

    @GetMapping("/status/{targetUserId}")
    public ResponseEntity<ApiResponse<FollowStatusDto>> checkFollowStatus(@AuthenticationPrincipal String userId,
                                             @PathVariable Long targetUserId) {
        return ApiResponse.success(SuccessStatus.SEARCH_STATUS_FOLLOW_SUCCESS, followService.checkFollowStatus(Long.valueOf(userId), targetUserId));
    }
}
