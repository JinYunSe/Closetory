package com.ssafy.closetory.repository;

import com.ssafy.closetory.entity.post.Comment;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Integer> {

  Integer countByPostId(Integer postId);

  @EntityGraph(attributePaths = {"user"})
  List<Comment> findAllByPostIdAndDeletedAtIsNullOrderByCreatedAtAsc(Integer postId);
}
