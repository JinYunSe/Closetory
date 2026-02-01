package com.ssafy.closetory.repository;

import com.ssafy.closetory.entity.post.Likes;
import com.ssafy.closetory.entity.post.LikesId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikesRepository extends JpaRepository<Likes, LikesId> {
  Integer countByPostId(Integer postId);

  boolean existsByUserIdAndPostId(Integer userId, Integer postId);
}
