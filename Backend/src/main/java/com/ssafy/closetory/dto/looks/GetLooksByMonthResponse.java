package com.ssafy.closetory.dto.looks;

import com.ssafy.closetory.enums.ClothesColor;
import java.time.LocalDate;
import lombok.Builder;

@Builder
public record GetLooksByMonthResponse(
    Integer lookId,
    LocalDate date,
    String photoUrl,
    ClothesColor topColor,
    ClothesColor bottomColor) {}
