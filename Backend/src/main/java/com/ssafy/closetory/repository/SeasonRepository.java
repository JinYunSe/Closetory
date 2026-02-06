package com.ssafy.closetory.repository;

import com.ssafy.closetory.entity.clothes.Season;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeasonRepository extends JpaRepository<Season, Integer> {}
