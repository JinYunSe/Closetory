package com.ssafy.closetory.dto.clothes;

import com.ssafy.closetory.entity.clothes.Tag;

public record TagItem(Integer tagId, String tagName) {
  public static TagItem from(Tag tag) {
    return new TagItem(tag.getId(), tag.getTagName());
  }
}
