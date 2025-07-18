package com.hamss2.KINO.api.follow.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FollowUserDto {
    private Long userId;
    private String nickname;
}
