package com.hamss2.KINO.api.admin.repository;

import com.hamss2.KINO.api.entity.UserBan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserBanRepoitory extends JpaRepository<UserBan, Long> {
}
