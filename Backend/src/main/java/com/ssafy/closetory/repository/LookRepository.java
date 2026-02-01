package com.ssafy.closetory.repository;

import com.ssafy.closetory.entity.looks.Look;
import com.ssafy.closetory.repository.projection.Top3Row;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LookRepository extends JpaRepository<Look, Integer> {
  List<Look> findAllByUserIdOrderByCreatedAtDesc(Integer userId);

  List<Look> findAllByUserIdAndDateLessThanEqualOrderByDateAsc(Integer userId, LocalDate endDate);

  @Query(
      value =
          """
    SELECT
      c.id AS clothesId,
      c.photo_url AS photoUrl,
      COUNT(*) AS usageCount
    FROM looks l
    JOIN clothes_looks cl ON cl.look_id = l.id
    JOIN clothes c ON c.id = cl.clothes_id
    WHERE l.user_id = :userId
      AND l.date >= :startDate
      AND l.date <  :endDate
    GROUP BY c.id, c.photo_url
    ORDER BY usageCount DESC
    LIMIT 3
  """,
      nativeQuery = true)
  List<Top3Row> findTop3ThisMonth(
      @Param("userId") Integer userId,
      @Param("startDate") LocalDate startDate,
      @Param("endDate") LocalDate endDate);
}
