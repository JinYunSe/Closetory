package com.ssafy.closetory.repository;

import com.ssafy.closetory.entity.Cloth;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClothRepository extends JpaRepository<Cloth, Long> {
  List<Cloth> findByOwner_Id(Long ownerId);
}
