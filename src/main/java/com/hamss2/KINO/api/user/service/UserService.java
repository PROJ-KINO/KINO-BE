package com.hamss2.KINO.api.user.service;

import com.hamss2.KINO.api.entity.User;
import com.hamss2.KINO.api.testPackage.UserRepository;
import com.hamss2.KINO.api.user.dto.SimpleUserReqDto;
import com.hamss2.KINO.common.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public SimpleUserReqDto getSimpleUserInfo(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("존재하지 않는 사용자입니다."));

        return SimpleUserReqDto.builder()
            .userId(user.getUserId())
            .nickname(user.getNickname())
            .image(user.getImage())
            .email(user.getEmail())
            .isFirstLogin(user.getIsFirstLogin())
            .build();
    }
}
