package com.hamss2.KINO.api.home.repository;

import com.hamss2.KINO.api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

}
