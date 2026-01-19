package com.ssafy.closetory.service.cloth;

import com.ssafy.closetory.dto.cloth.ClosetClothItem;
import com.ssafy.closetory.dto.cloth.GetClosetRequest;
import com.ssafy.closetory.dto.cloth.GetClosetResponse;
import com.ssafy.closetory.entity.Cloth;
import com.ssafy.closetory.enums.ClothColor;
import com.ssafy.closetory.exception.common.BadRequestException;
import com.ssafy.closetory.repository.ClothRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClothServiceImpl implements ClothService {
  private final ClothRepository clothRepository;

  @Override
  public GetClosetResponse getCloset(Long userId, GetClosetRequest request) {
    List<Cloth> clothes = clothRepository.findByOwner_Id(userId);

    String color = (request == null) ? null : request.color();
    if (color != null && !color.isBlank()) {
      ClothColor filterColor;
      try {
        filterColor = ClothColor.valueOf(color);
      } catch (IllegalArgumentException e) {
        throw new BadRequestException("color 값이 올바르지 않습니다. 예: BLACK, WHITE, RED");
      }

      clothes = clothes.stream().filter(c -> c.getColor() == filterColor).toList();
    }

    List<ClosetClothItem> top = new ArrayList<>();
    List<ClosetClothItem> bottom = new ArrayList<>();
    List<ClosetClothItem> accessories = new ArrayList<>();
    List<ClosetClothItem> bags = new ArrayList<>();
    List<ClosetClothItem> outer = new ArrayList<>();

    for (Cloth c : clothes) {
      ClosetClothItem item = new ClosetClothItem(c.getClothId(), c.getClothImage());

      switch (c.getClothType()) {
        case TOP -> top.add(item);
        case BOTTOM -> bottom.add(item);
        case ACCESSORIES -> accessories.add(item);
        case BAG -> bags.add(item);
        case OUTER -> outer.add(item);
      }
    }

    return new GetClosetResponse(top, bottom, accessories, bags, outer);
  }
}
