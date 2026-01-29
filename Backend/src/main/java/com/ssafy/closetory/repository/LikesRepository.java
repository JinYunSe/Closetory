package com.ssafy.closetory.repository;

import com.ssafy.closetory.entity.post.Likes;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikesRepository extends JpaRepository<Likes, Integer> {}
