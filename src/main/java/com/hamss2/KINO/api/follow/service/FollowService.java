package com.hamss2.KINO.api.follow.service;

import com.hamss2.KINO.api.follow.dto.FollowStatusDto;
import com.hamss2.KINO.api.follow.dto.FollowUserDto;
import com.hamss2.KINO.api.entity.Follow;
import com.hamss2.KINO.api.entity.User;
import com.hamss2.KINO.api.follow.repository.FollowRepository;

import com.hamss2.KINO.api.testPackage.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    @Transactional
    public void follow(Long followerId, Long followeeId) {
        if (followerId.equals(followeeId)) {
            throw new IllegalArgumentException("자기 자신을 팔로우할 수 없습니다.");
        }

        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new IllegalArgumentException("팔로워가 존재하지 않습니다."));
        User followee = userRepository.findById(followeeId)
                .orElseThrow(() -> new IllegalArgumentException("팔로우 대상이 존재하지 않습니다."));

        if (!followRepository.existsByFollowerAndFollowee(follower, followee)) {
            followRepository.save(new Follow(null, null, follower, followee));
        }
    }

    @Transactional
    public void unfollow(Long followerId, Long followeeId) {
        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new IllegalArgumentException("팔로워가 존재하지 않습니다."));
        User followee = userRepository.findById(followeeId)
                .orElseThrow(() -> new IllegalArgumentException("팔로우 대상이 존재하지 않습니다."));

        followRepository.deleteByFollowerAndFollowee(follower, followee);
    }

    @Transactional(readOnly = true)
    public List<FollowUserDto> getFollowers(Long userId, Long targetId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자가 존재하지 않습니다."));

        User target = userRepository.findById(targetId)
                .orElseThrow(() -> new IllegalArgumentException("사용자가 존재하지 않습니다."));

        boolean isFollow = followRepository.existsByFollowerAndFollowee(user, target);

        return followRepository.findAllByFollowee(user).stream()
                .map(f -> new FollowUserDto(f.getFollower().getUserId(), f.getFollower().getNickname(), isFollow))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FollowUserDto> getFollowings(Long userId, Long targetId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자가 존재하지 않습니다."));

        User target = userRepository.findById(targetId)
                .orElseThrow(() -> new IllegalArgumentException("사용자가 존재하지 않습니다."));

        boolean isFollow = followRepository.existsByFollowerAndFollowee(user, target);

        return followRepository.findAllByFollower(user).stream()
                .map(f -> new FollowUserDto(f.getFollowee().getUserId(), f.getFollowee().getNickname(), isFollow))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public FollowStatusDto checkFollowStatus(Long myId, Long targetUserId) {
        User me = userRepository.findById(myId)
                .orElseThrow(() -> new IllegalArgumentException("사용자가 존재하지 않습니다."));
        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("대상이 존재하지 않습니다."));

        boolean status = followRepository.existsByFollowerAndFollowee(me, target);
        return new FollowStatusDto(status);
    }
}
