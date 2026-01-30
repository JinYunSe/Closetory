package com.ssafy.closetory.repository;

import com.ssafy.closetory.entity.looks.ClothesLooks;
import com.ssafy.closetory.entity.looks.ClothesLooksId;
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
}
