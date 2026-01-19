package com.ssafy.closetory.dto.cloth;

import java.util.List;

public record GetClosetResponse(
    List<ClosetClothItem> topClothes,
    List<ClosetClothItem> bottomClothes,
    List<ClosetClothItem> accessories,
    List<ClosetClothItem> bags,
    List<ClosetClothItem> outerClothes) {}
