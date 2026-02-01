package com.ssafy.closetory.repository;

import com.ssafy.closetory.entity.post.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Integer> {

  Integer countByPostId(Integer postId);
}
