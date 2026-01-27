package com.ssafy.closetory.repository;

import com.ssafy.closetory.entity.post.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Integer> {}
