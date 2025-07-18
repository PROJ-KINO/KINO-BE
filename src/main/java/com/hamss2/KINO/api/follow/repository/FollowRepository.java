package com.hamss2.KINO.api.follow.repository;

import com.hamss2.KINO.api.entity.Follow;
import com.hamss2.KINO.api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FollowRepository extends JpaRepository<Follow, Long> {
    boolean existsByFollowerAndFollowee(User follower, User followee);
    void deleteByFollowerAndFollowee(User follower, User followee);

    List<Follow> findAllByFollower(User follower); // 내가 팔로우한 사람들
    List<Follow> findAllByFollowee(User followee); // 나를 팔로우한 사람들
}