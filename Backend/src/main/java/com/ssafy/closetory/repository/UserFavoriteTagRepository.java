package com.ssafy.closetory.repository;

import com.ssafy.closetory.entity.user.UserFavoriteTag;
import com.ssafy.closetory.entity.user.UserFavoriteTagId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserFavoriteTagRepository
    extends JpaRepository<UserFavoriteTag, UserFavoriteTagId> {
  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query("delete from UserFavoriteTag u where u.id.userId = :userId")
  void deleteByIdUserId(@Param("userId") Integer userId);
}
