package com.ssafy.closetory.repository;

import com.ssafy.closetory.entity.clothes.Tag;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, Integer> {
  List<Tag> findAllByOrderByIdAsc();
}
