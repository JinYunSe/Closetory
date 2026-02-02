package com.ssafy.closetory.service.clothes;

import com.ssafy.closetory.dto.clothes.TagItem;
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
  public List<TagItem> getTags() {
    return tagRepository.findAll().stream().map(TagItem::from).toList();
  }
}
