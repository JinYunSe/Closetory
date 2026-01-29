package com.ssafy.closetory.dto.clothes;

import com.ssafy.closetory.enums.ClothesColor;
import com.ssafy.closetory.enums.ClothesType;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record AddClothesRequest(
    @NotNull String photoUrl,
    @NotNull List<Integer> tags,
    @NotNull ClothesType clothesType,
    @NotNull List<Integer> seasons,
    @NotNull ClothesColor color) {}
