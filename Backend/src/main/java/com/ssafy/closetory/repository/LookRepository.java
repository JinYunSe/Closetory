package com.ssafy.closetory.repository;

import com.ssafy.closetory.entity.looks.Look;
import com.ssafy.closetory.repository.projection.StatsRow;
import com.ssafy.closetory.repository.projection.Top3Row;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;


public interface LookRepository extends JpaRepository<Look, Integer> {
  List<Look> findAllByUserIdOrderByCreatedAtDesc(Integer userId);

  List<Look> findAllByUserIdAndDateLessThanEqualOrderByDateAsc(
      Integer userId, LocalDate endDate);

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

  @Query(
      value =
          """
    SELECT
      t.tag_name AS name,
      ROUND(COUNT(*) / SUM(COUNT(*)) OVER () * 100, 2) AS percentage
    FROM looks l
    JOIN clothes_looks cl ON cl.look_id = l.id
    JOIN tags_clothes tc  ON tc.clothes_id = cl.clothes_id
    JOIN tags t           ON t.id = tc.tag_id
    WHERE l.user_id = :userId
      AND l.date >= :startDate
      AND l.date <  :endDate
    GROUP BY t.id, t.tag_name
    ORDER BY COUNT(*) DESC, t.id
    """,
      nativeQuery = true)
  List<StatsRow> findTagStatsThisMonth(
      @Param("userId") Integer userId,
      @Param("startDate") LocalDate startDate,
      @Param("endDate") LocalDate endDate);

  @Query(
      value =
          """
    SELECT
      c.color AS name,
      ROUND(COUNT(*) / SUM(COUNT(*)) OVER () * 100, 2) AS percentage
    FROM looks l
    JOIN clothes_looks cl ON cl.look_id = l.id
    JOIN clothes c        ON c.id = cl.clothes_id
    WHERE l.user_id = :userId
      AND l.date >= :startDate
      AND l.date <  :endDate
    GROUP BY c.color
    ORDER BY COUNT(*) DESC, c.color
    """,
      nativeQuery = true)
  List<StatsRow> findColorStatsThisMonth(
      @Param("userId") Integer userId,
      @Param("startDate") LocalDate startDate,
      @Param("endDate") LocalDate endDate);
}
