package com.ssafy.closetory.repository;

import com.ssafy.closetory.entity.clothes.Save;
import com.ssafy.closetory.entity.clothes.SaveId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SaveRepository extends JpaRepository<Save, SaveId> {

  boolean existsByUserIdAndClothesId(Integer userId, Integer clothesId);
}
