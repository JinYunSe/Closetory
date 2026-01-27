package com.ssafy.closetory.repository;

import com.ssafy.closetory.entity.clothes.Clothes;
import com.ssafy.closetory.enums.ClothesColor;
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
}
