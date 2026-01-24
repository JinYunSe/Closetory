package com.ssafy.closetory.dto.clothes;

import com.ssafy.closetory.enums.ClothesColor;
import com.ssafy.closetory.enums.ClothesType;
import java.util.List;

public record AddClothesRequest(
    List<Integer> tags, ClothesType clothesType, List<Integer> seasons, ClothesColor color) {}
