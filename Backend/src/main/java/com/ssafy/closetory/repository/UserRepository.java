package com.ssafy.closetory.repository;

import com.ssafy.closetory.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

  boolean existsByUserId(String id);

  boolean existsByNickname(String nickname);

}
