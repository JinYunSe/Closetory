package com.ssafy.closetory.repository;

import com.ssafy.closetory.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

  boolean existsByUserId(String id);

  boolean existsByNickname(String nickname);

  Optional<User> findByUserId(String userId);
}
