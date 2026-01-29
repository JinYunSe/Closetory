package com.ssafy.closetory.repository;

import com.ssafy.closetory.entity.post.Post;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Integer> {
  @Query(
      """
      SELECT l.post
      FROM Likes l
      JOIN l.post
      WHERE l.user.id = :userId and l.post.deletedAt IS NULL
      ORDER BY (l.post.views * 0.5 + CAST(function('DATEDIFF', CURRENT_DATE, l.createdAt) AS INTEGER) * -1 * 10) DESC
      """)
  List<Post> findLikedPostsByUserId(@Param("userId") Integer userId, Pageable pageable);

  @Query(
      """
    SELECT p
    FROM Post p
    LEFT JOIN Likes l ON l.post = p
    WHERE p.userId = :userId and l.post.deletedAt IS NULL
    GROUP BY p
    ORDER BY (
        (COUNT(l) * 100.0) / (p.views + 10) +
        (CAST(function('DATEDIFF', CURRENT_DATE, p.createdAt) AS INTEGER) * -0.5)
    ) DESC
    """)
  List<Post> findWrittenPostsByUserId(@Param("userId") Integer userId, Pageable pageable);

  @Query(
      """
      SELECT p
      FROM Post p
      WHERE p.deletedAt IS NULL
      ORDER BY (p.views * 0.5 + CAST(function('DATEDIFF', CURRENT_DATE, p.createdAt) AS INTEGER) * -1 * 10) DESC
      """)
  List<Post> findPostsByViews(Pageable pageable);
}
