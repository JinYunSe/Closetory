package com.ssafy.closetory.service.cloth;

import com.ssafy.closetory.dto.cloth.ClosetClothItem;
import com.ssafy.closetory.dto.cloth.GetClosetRequest;
import com.ssafy.closetory.dto.cloth.GetClosetResponse;
import com.ssafy.closetory.entity.cloth.Cloth;
import com.ssafy.closetory.enums.ClothColor;
import com.ssafy.closetory.enums.Season;
import com.ssafy.closetory.exception.common.BadRequestException;
import com.ssafy.closetory.repository.ClothRepository;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClothServiceImpl implements ClothService {

  private final ClothRepository clothRepository;

  @Override
  public GetClosetResponse getCloset(Long userId, GetClosetRequest request) {

    boolean onlyLike = request != null && Boolean.TRUE.equals(request.onlyLike());
    boolean onlyMine = request != null && Boolean.TRUE.equals(request.onlyMine());

    ClothColor color = null;
    if (request != null && request.color() != null && !request.color().isBlank()) {
      try {
        color = ClothColor.valueOf(request.color().trim().toUpperCase(Locale.ROOT));
      } catch (IllegalArgumentException e) {
        throw new BadRequestException("color 값이 올바르지 않습니다. 예: BLACK, WHITE, RED");
      }
    }

    List<Season> seasons = List.of();
    boolean seasonsEmpty = true;
    if (request != null && request.seasons() != null && !request.seasons().isEmpty()) {
      seasons =
          request.seasons().stream()
              .filter(s -> s != null && !s.isBlank())
              .map(
                  s -> {
                    try {
                      return Season.valueOf(s.trim().toUpperCase(Locale.ROOT));
                    } catch (IllegalArgumentException e) {
                      throw new BadRequestException(
                          "season 값이 올바르지 않습니다. 예: SPRING, SUMMER, FALL, WINTER");
                    }
                  })
              .distinct()
              .toList();
      seasonsEmpty = seasons.isEmpty();
    }

    List<String> tags = List.of();
    boolean tagsEmpty = true;
    if (request != null && request.tags() != null && !request.tags().isEmpty()) {
      tags =
          request.tags().stream()
              .filter(t -> t != null && !t.isBlank())
              .map(String::trim)
              .distinct()
              .toList();
      tagsEmpty = tags.isEmpty();
    }

    List<Cloth> clothes =
        clothRepository.searchCloset(
            userId, onlyMine, onlyLike, color, seasons, seasonsEmpty, tags, tagsEmpty);

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
