package com.ssafy.closetory.dto.cloth;

import java.util.List;

public record GetClosetRequest(
    List<Integer> tagIds, List<Integer> seasonIds, String color, Boolean onlyMine) {}
