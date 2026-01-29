package com.ssafy.closetory.dto.looks;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

public record FastApiResponse(
    @JsonProperty("selections") Map<String, Integer> selections,
    @JsonProperty("reason") String reason) {}
