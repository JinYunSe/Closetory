package com.ssafy.closetory.repository;

import com.ssafy.closetory.entity.cloth.Cloth;
import com.ssafy.closetory.enums.ClothColor;
import com.ssafy.closetory.enums.Season;
import java.util.List;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface ClothRepository extends JpaRepository<Cloth, Long> {

  @Query(
      """
    select c
    from Cloth c
    where
      (
        (:onlyMine = true and c.owner.id = :userId)
        or
        (:onlyMine = false and (
            c.owner.id = :userId
            or exists (
              select 1
              from ClothSave s
              where s.id.userId = :userId
                and s.id.clothId = c.clothId
            )
        ))
      )

      and (
        :onlyLike = false
        or exists (
          select 1
          from ClothLike l
          where l.id.userId = :userId
            and l.id.clothId = c.clothId
        )
      )

      and (
        :color is null
        or c.color = :color
      )

      and (
        :seasonsEmpty = true
        or exists (
          select 1
          from ClothSeason cs
          where cs.id.clothId = c.clothId
            and cs.id.season in :seasons
        )
      )

      and (
        :tagsEmpty = true
        or exists (
          select 1
          from ClothTag t
          where t.id.clothId = c.clothId
            and t.id.tag in :tags
        )
      )
  """)
  List<Cloth> searchCloset(
      @Param("userId") Long userId,
      @Param("onlyMine") boolean onlyMine,
      @Param("onlyLike") boolean onlyLike,
      @Param("color") ClothColor color,
      @Param("seasons") List<Season> seasons,
      @Param("seasonsEmpty") boolean seasonsEmpty,
      @Param("tags") List<String> tags,
      @Param("tagsEmpty") boolean tagsEmpty);
}
