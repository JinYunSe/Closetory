package com.ssafy.closetory.entity.user;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "user_favorite_tags")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserFavoriteTag {

  @EmbeddedId private UserFavoriteTagId id;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  public static UserFavoriteTag of(Integer userId, Integer tagId) {
    UserFavoriteTag e = new UserFavoriteTag();
    e.id = new UserFavoriteTagId(userId, tagId);
    return e;
  }
}
