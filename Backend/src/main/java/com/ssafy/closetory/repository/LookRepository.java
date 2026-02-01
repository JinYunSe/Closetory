package com.ssafy.closetory.repository;

import com.ssafy.closetory.entity.looks.Look;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LookRepository extends JpaRepository<Look, Integer> {
  List<Look> findAllByUserIdOrderByCreatedAtDesc(Integer userId);

  List<Look> findAllByUserIdAndDateBetweenOrderByDateAsc(
      Integer userId, LocalDate startDate, LocalDate endDate);
}
