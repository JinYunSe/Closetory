package com.ssafy.closetory.dto.clothes;

import com.ssafy.closetory.entity.clothes.TagItem;
import java.util.List;

public record GetTagsResponse(List<TagItem> tags) {}
