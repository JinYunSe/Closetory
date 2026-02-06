package com.ssafy.closetory.repository;

import com.ssafy.closetory.entity.user.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<User, Integer> {

  boolean existsByUsername(String username);

  boolean existsByNickname(String nickname);

  Optional<User> findByUsernameAndDeletedAtIsNull(String username);

  @Query("select u.nickname from User u where u.id = :userId")
  String findNicknameById(Integer userId);
}
