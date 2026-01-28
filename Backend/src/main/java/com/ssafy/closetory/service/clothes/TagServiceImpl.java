package com.ssafy.closetory.service.clothes;

import com.ssafy.closetory.dto.clothes.GetTagsResponse;
import com.ssafy.closetory.entity.clothes.TagItem;
import com.ssafy.closetory.repository.TagRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TagServiceImpl implements TagService {
  private final TagRepository tagRepository;

  @Override
  public GetTagsResponse getTags() {
    List<TagItem> list = tagRepository.findAll().stream().map(TagItem::from).toList();

    return new GetTagsResponse(list);
  }
}
