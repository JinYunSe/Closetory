package com.ssafy.closetory.dto.cloth;

import java.util.List;

public record GetClosetRequest(
    List<String> tags, String color, List<String> seasons, Boolean onlyLike, Boolean onlyMine) {}
