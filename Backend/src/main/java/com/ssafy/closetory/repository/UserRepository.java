package com.ssafy.closetory.repository;

import com.ssafy.closetory.entity.user.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {

  boolean existsByUsername(String username);

  boolean existsByNickname(String nickname);

  Optional<User> findByUsernameAndDeletedAtIsNull(String username);
}
