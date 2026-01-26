package com.ssafy.closetory.repository;

import com.ssafy.closetory.entity.user.UserFavoriteTag;
import com.ssafy.closetory.entity.user.UserFavoriteTagId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserFavoriteTagRepository
    extends JpaRepository<UserFavoriteTag, UserFavoriteTagId> {
  boolean existsByIdUserId(Integer userId);
}
