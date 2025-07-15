package com.hamss2.KINO.api.mypage.repository;

import com.hamss2.KINO.api.entity.UserGenre;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserGenreRepository extends JpaRepository<UserGenre, Long> {
    void deleteByUser_UserId(Long userId);
} 