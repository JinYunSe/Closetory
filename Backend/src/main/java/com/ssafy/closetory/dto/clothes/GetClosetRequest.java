package com.ssafy.closetory.dto.clothes;

import java.util.List;

public record GetClosetRequest(
    List<Integer> tagIds, List<Integer> seasonIds, String color, Boolean onlyMine) {}
