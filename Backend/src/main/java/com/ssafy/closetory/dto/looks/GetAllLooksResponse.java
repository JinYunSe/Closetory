package com.ssafy.closetory.dto.looks;

import java.time.LocalDate;
import lombok.Builder;

@Builder
public record GetAllLooksResponse(
    Integer lookId, String photoUrl, LocalDate date, String aiReason, Boolean onlyMine) {}
