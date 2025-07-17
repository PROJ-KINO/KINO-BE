package com.hamss2.KINO.api.auth.repository;

import com.hamss2.KINO.api.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TokenRepository extends JpaRepository<RefreshToken, Long> {

    Boolean existsByUserUserId(Long userId);

    RefreshToken findByUserUserId(Long userId);

    void deleteByUserUserId(Long userId);

}
