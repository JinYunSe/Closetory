package com.ssafy.closetory.repository;

import com.ssafy.closetory.entity.looks.ClothesLooks;
import com.ssafy.closetory.entity.looks.ClothesLooksId;
import com.ssafy.closetory.enums.ClothesType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClothesLooksRepository extends JpaRepository<ClothesLooks, ClothesLooksId> {
  @Query(
      """
    SELECT cl
    FROM ClothesLooks cl
    JOIN FETCH cl.clothes
    WHERE cl.id.lookId IN :lookIds
  """)
  List<ClothesLooks> findByIdLookIdIn(@Param("lookIds") List<Integer> lookIds);

  @Query(
      """
  SELECT cl
  FROM ClothesLooks cl
  JOIN FETCH cl.clothes c
  WHERE cl.id.lookId IN :lookIds
  AND c.clothesType IN :types
""")
  List<ClothesLooks> findTopBottomByIdLookIdIn(
      @Param("lookIds") List<Integer> lookIds, @Param("types") List<ClothesType> types);
}
