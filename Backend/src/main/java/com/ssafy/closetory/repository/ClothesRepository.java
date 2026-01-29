package com.ssafy.closetory.repository;

import com.ssafy.closetory.entity.clothes.Clothes;
import com.ssafy.closetory.enums.ClothesColor;
import com.ssafy.closetory.repository.projection.ClothesRecommendRow;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface ClothesRepository extends JpaRepository<Clothes, Integer> {

  @Query(
      """
    select distinct c
    from Clothes c
    where (
        (c.userId = :userId and c.deletedAt is null)
        or
        (:onlyMine = false and c.userId <> :userId and exists (
            select 1
            from Save s
            where s.user.id = :userId
              and s.clothes.id = c.id
        ))
      )

      and (
        :color is null
        or c.color = :color
      )

      and (
        :seasonIdsEmpty = true
        or exists (
          select 1
          from Clothes c2
          join c2.seasons se
          where c2.id = c.id
            and se.id in :seasonIds
        )
      )

      and (
        :tagIdsEmpty = true
        or exists (
          select 1
          from Clothes c3
          join c3.tags t
          where c3.id = c.id
            and t.id in :tagIds
        )
      )
  """)
  List<Clothes> searchCloset(
      @Param("userId") Integer userId,
      @Param("onlyMine") boolean onlyMine,
      @Param("color") ClothesColor color,
      @Param("seasonIds") List<Integer> seasonIds,
      @Param("seasonIdsEmpty") boolean seasonIdsEmpty,
      @Param("tagIds") List<Integer> tagIds,
      @Param("tagIdsEmpty") boolean tagIdsEmpty);

  Optional<Clothes> getClothesById(Integer id);

  Optional<Clothes> findByIdAndDeletedAtIsNull(Integer id);

  @Query(
      value =
          """
    WITH
    target_looks AS (
      SELECT DISTINCT cl.look_id
      FROM clothes_looks cl
      JOIN looks l ON l.id = cl.look_id
      WHERE cl.clothes_id = :targetClothesId
        AND l.user_id = :userId
    ),
    totalA AS (
      SELECT COUNT(*) AS cnt FROM target_looks
    ),
    co_wear AS (
      SELECT
        b.clothes_id AS cand_id,
        COUNT(DISTINCT b.look_id) AS co_cnt
      FROM clothes_looks a
      JOIN clothes_looks b ON b.look_id = a.look_id
      JOIN looks l ON l.id = a.look_id
      WHERE a.clothes_id = :targetClothesId
        AND b.clothes_id <> :targetClothesId
        AND l.user_id = :userId
      GROUP BY b.clothes_id
    ),
    tag_overlap AS (
      SELECT
        tc2.clothes_id AS cand_id,
        COUNT(DISTINCT tc2.tag_id) AS tag_cnt
      FROM tags_clothes tc1
      JOIN tags_clothes tc2 ON tc2.tag_id = tc1.tag_id
      WHERE tc1.clothes_id = :targetClothesId
        AND tc2.clothes_id <> :targetClothesId
      GROUP BY tc2.clothes_id
    ),
    last_worn AS (
      SELECT
        cl.clothes_id AS cand_id,
        MAX(l.date) AS last_date
      FROM clothes_looks cl
      JOIN looks l ON l.id = cl.look_id
      WHERE l.user_id = :userId
      GROUP BY cl.clothes_id
    ),
    scored AS (
      SELECT
        c.id AS clothes_id,
        c.photo_url AS photo_url,
        c.clothes_type AS clothes_type,

        IFNULL(t.tag_cnt, 0) AS tagScore,

        CASE
          WHEN (SELECT cnt FROM totalA) = 0 THEN 0
          ELSE ROUND(10 * IFNULL(w.co_cnt, 0) / (SELECT cnt FROM totalA))
        END AS freqScore,

        CASE
          WHEN lw.last_date IS NULL THEN 0
          WHEN DATEDIFF(CURDATE(), lw.last_date) BETWEEN 1 AND 7
            THEN -(8 - DATEDIFF(CURDATE(), lw.last_date))
          ELSE 0
        END AS recentPenalty

      FROM clothes c
      LEFT JOIN tag_overlap t ON t.cand_id = c.id
      LEFT JOIN co_wear w ON w.cand_id = c.id
      LEFT JOIN last_worn lw ON lw.cand_id = c.id

      WHERE c.user_id = :userId
        AND c.deleted_at IS NULL
        AND c.id <> :targetClothesId

        -- 선택한 옷과 동일 카테고리 제외
        AND c.clothes_type <> (SELECT clothes_type FROM clothes WHERE id = :targetClothesId)

        -- 가방/악세서리는 항상 제외
        AND c.clothes_type NOT IN ('BAG', 'ACCESSORIES')

        -- 같은 계절인 옷만
        AND EXISTS (
          SELECT 1
          FROM clothes_seasons cs
          WHERE cs.clothes_id = c.id
            AND cs.season_id IN (:seasonIds)
        )

    ),
    ranked AS (
      SELECT
        clothes_id,
        photo_url,
        clothes_type,
        (tagScore + freqScore + recentPenalty) AS score,
        freqScore,
        tagScore,
        recentPenalty,
        ROW_NUMBER() OVER (
          PARTITION BY clothes_type
          ORDER BY (tagScore + freqScore + recentPenalty) DESC,
                   freqScore DESC,
                   tagScore DESC,
                   recentPenalty DESC,
                   clothes_id ASC
        ) AS rn
      FROM scored
    )
    SELECT
      clothes_id AS clothesId,
      photo_url AS photoUrl
    FROM ranked
    WHERE rn <= 2
    ORDER BY score DESC
    """,
      nativeQuery = true)
  List<ClothesRecommendRow> recommendTopByCategory(
      @Param("userId") Integer userId,
      @Param("targetClothesId") Integer targetClothesId,
      @Param("seasonIds") List<Integer> seasonIds);
}
