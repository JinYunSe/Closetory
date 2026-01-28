package com.ssafy.closetory.dto.post;

import java.util.List;

public record PostUpdateRequest(String title, String content, List<Integer> items) {}
