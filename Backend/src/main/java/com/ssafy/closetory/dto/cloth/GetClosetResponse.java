package com.ssafy.closetory.dto.cloth;

import java.util.List;

public record GetClosetResponse(
    List<ClosetClothesItem> topClothes,
    List<ClosetClothesItem> bottomClothes,
    List<ClosetClothesItem> accessories,
    List<ClosetClothesItem> bags,
    List<ClosetClothesItem> outerClothes) {}
