package com.hamss2.KINO.api.follow.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FollowRequestDto {
    private Long targetUserId; // 팔로우 대상
}
