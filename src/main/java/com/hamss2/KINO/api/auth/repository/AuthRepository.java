package com.hamss2.KINO.api.auth.repository;

import com.hamss2.KINO.api.auth.dto.SocialType;
import com.hamss2.KINO.api.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthRepository extends JpaRepository<User, Long> {

    Optional<User> findByUserId(Long userId);

    User findByEmail(String email);

    boolean existsByEmail(String email); // 사용자가 이메일로 가입했는지 확인

    Optional<User> findBySocialTypeAndProviderId(SocialType socialType, String providerId);

    boolean existsBySocialTypeAndProviderId(SocialType socialType, String providerId);

}
