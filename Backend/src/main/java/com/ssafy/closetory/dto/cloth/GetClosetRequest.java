package com.ssafy.closetory.dto.cloth;

import java.util.List;

public record GetClosetRequest(
    List<String> tags, String color, List<String> seasons, Boolean like, Boolean ownedCloth) {}
