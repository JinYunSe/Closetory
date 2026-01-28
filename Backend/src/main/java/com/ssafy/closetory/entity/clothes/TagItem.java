package com.ssafy.closetory.entity.clothes;

public record TagItem(Integer tagId, String tagName) {
  public static TagItem from(Tag tag) {
    return new TagItem(tag.getId(), tag.getTagName());
  }
}
