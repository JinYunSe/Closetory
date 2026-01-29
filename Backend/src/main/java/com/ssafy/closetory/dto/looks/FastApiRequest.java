package com.ssafy.closetory.dto.looks;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Builder;

@Builder
public record FastApiRequest(
    @JsonProperty("ref_image") String refImage,
    @JsonProperty("top_image_list") List<String> tops,
    @JsonProperty("bottom_image_list") List<String> bottoms,
    @JsonProperty("shoes_image_list") List<String> shoes,
    @JsonProperty("outer_image_list") List<String> outers,
    @JsonProperty("accessory_image_list") List<String> accessories,
    @JsonProperty("bag_image_list") List<String> bags) {}
