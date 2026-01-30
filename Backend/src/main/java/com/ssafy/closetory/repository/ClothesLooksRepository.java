package com.ssafy.closetory.repository;

import com.ssafy.closetory.entity.looks.ClothesLooks;
import com.ssafy.closetory.entity.looks.ClothesLooksId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClothesLooksRepository extends JpaRepository<ClothesLooks, ClothesLooksId> {}
