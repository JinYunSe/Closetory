package com.ssafy.closetory.service.clothes;

import com.ssafy.closetory.dto.clothes.*;
import com.ssafy.closetory.entity.clothes.*;
import com.ssafy.closetory.entity.user.User;
import com.ssafy.closetory.enums.ClothesColor;
import com.ssafy.closetory.exception.common.BadRequestException;
import com.ssafy.closetory.exception.common.ConflictException;
import com.ssafy.closetory.exception.common.ForbiddenException;
import com.ssafy.closetory.exception.common.NotFoundException;
import com.ssafy.closetory.repository.*;
import com.ssafy.closetory.repository.projection.ClothesRecommendRow;
import com.ssafy.closetory.service.s3.S3ImageService;
import java.time.LocalDateTime;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClothesServiceImpl implements ClothesService {

  private final ClothesRepository clothesRepository;
  private final TagRepository tagRepository;
  private final SeasonRepository seasonRepository;
  private final SaveRepository saveRepository;
  private final UserRepository userRepository;
  private final S3ImageService s3ImageService;
  private final WebClient fastApiWebClient;

  @Override
  public GetClosetResponse getCloset(Integer userId, GetClosetRequest request) {

    boolean onlyMine = request.onlyMine() != null ? request.onlyMine() : true;

    List<Integer> tagIds = request.tagIds() != null ? request.tagIds() : List.of();
    List<Integer> seasonIds = request.seasonIds() != null ? request.seasonIds() : List.of();

    boolean tagIdsEmpty = tagIds.isEmpty();
    boolean seasonIdsEmpty = seasonIds.isEmpty();

    ClothesColor color = parseColorOrNull(request.color());

    List<Clothes> closet =
        clothesRepository.searchCloset(
            userId, onlyMine, color, seasonIds, seasonIdsEmpty, tagIds, tagIdsEmpty);

    List<ClosetClothesItem> top = new ArrayList<>();
    List<ClosetClothesItem> bottom = new ArrayList<>();
    List<ClosetClothesItem> accessories = new ArrayList<>();
    List<ClosetClothesItem> bags = new ArrayList<>();
    List<ClosetClothesItem> outer = new ArrayList<>();
    List<ClosetClothesItem> shoes = new ArrayList<>();

    for (Clothes c : closet) {
      boolean isMine = userId.equals(c.getUserId());
      ClosetClothesItem item = ClosetClothesItem.of(c, isMine);

      switch (c.getClothesType()) {
        case TOP -> top.add(item);
        case BOTTOM -> bottom.add(item);
        case ACCESSORIES -> accessories.add(item);
        case BAG -> bags.add(item);
        case OUTER -> outer.add(item);
        case SHOES -> shoes.add(item);
      }
    }

    return new GetClosetResponse(top, bottom, accessories, bags, outer, shoes);
  }

  @Override
  public GetClothesDetailResponse getClothesDetail(Integer userId, Integer clothesId) {
    Clothes clothes =
        clothesRepository
            .getClothesById(clothesId)
            .orElseThrow(() -> new NotFoundException("존재하지 않는 옷입니다."));
    return GetClothesDetailResponse.from(clothes, userId);
  }

  @Transactional
  @Override
  public Integer addClothes(Integer userId, AddClothesRequest request) {
    Clothes clothes =
        Clothes.builder()
            .photoUrl(request.photoUrl())
            .clothesType(request.clothesType())
            .color(request.color())
            .userId(userId)
            .createdAt(LocalDateTime.now())
            .build();

    if (request.tags() != null && !request.tags().isEmpty()) {
      List<Integer> tagIds = request.tags().stream().distinct().toList();

      List<Tag> tags = tagRepository.findAllById(tagIds);
      if (tags.size() != tagIds.size()) {
        throw new NotFoundException("존재하지 않는 태그가 포함되어 있습니다.");
      }
      clothes.getTags().addAll(tags);
    }

    if (request.seasons() != null && !request.seasons().isEmpty()) {
      List<Integer> seasonIds = request.seasons().stream().distinct().toList();

      List<Season> seasons = seasonRepository.findAllById(seasonIds);
      if (seasons.size() != seasonIds.size()) {
        throw new NotFoundException("존재하지 않는 계절이 포함되어 있습니다.");
      }
      clothes.getSeasons().addAll(seasons);
    }

    Clothes saved = clothesRepository.save(clothes);
    return saved.getId();
  }

  @Transactional
  @Override
  public GetClothesDetailResponse updateClothes(
      Integer userId, Integer clothesId, UpdateClothesRequest req) {

    Clothes clothes =
        clothesRepository
            .findByIdAndDeletedAtIsNull(clothesId)
            .orElseThrow(() -> new NotFoundException("존재하지 않는 옷입니다."));

    if (!userId.equals(clothes.getUserId())) {
      throw new ForbiddenException("자신의 옷만 수정할 수 있습니다.");
    }

    if (req.photoUrl() != null) clothes.setPhotoUrl(req.photoUrl());
    if (req.clothesType() != null) clothes.setClothesType(req.clothesType());
    if (req.color() != null) clothes.setColor(req.color());
    if (req.tags() != null) {
      List<Integer> tagIds = req.tags().stream().distinct().toList();

      List<Tag> newTags = tagRepository.findAllById(tagIds);
      if (newTags.size() != tagIds.size()) {
        throw new BadRequestException("존재하지 않는 태그가 포함되어 있습니다.");
      }
      clothes.getTags().clear();
      clothes.getTags().addAll(newTags);
    }

    if (req.seasons() != null) {
      List<Integer> seasonIds = req.seasons().stream().distinct().toList();

      List<Season> newSeasons = seasonRepository.findAllById(seasonIds);
      if (newSeasons.size() != seasonIds.size()) {
        throw new BadRequestException("존재하지 않는 계절이 포함되어 있습니다.");
      }
      clothes.getSeasons().clear();
      clothes.getSeasons().addAll(newSeasons);
    }

    return GetClothesDetailResponse.from(clothes, userId);
  }

  @Transactional
  @Override
  public void deleteClothes(Integer userId, Integer clothesId) {
    Clothes clothes =
        clothesRepository
            .findById(clothesId)
            .orElseThrow(() -> new NotFoundException("존재하지 않는 옷입니다."));

    if (!clothes.getUserId().equals(userId)) {
      throw new ForbiddenException("자신의 옷만 삭제할 수 있습니다.");
    }

    clothes.setDeletedAt(LocalDateTime.now());
    clothesRepository.save(clothes);
  }

  private ClothesColor parseColorOrNull(String colorStr) {
    if (colorStr == null || colorStr.isBlank()) return null;

    try {
      return ClothesColor.valueOf(colorStr.trim().toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new BadRequestException("color 값이 올바르지 않습니다. 예: BLACK, WHITE, RED");
    }
  }

  @Override
  public String createMaskingImage(byte[] rawImage) {

    MultipartBodyBuilder builder = new MultipartBodyBuilder();

    builder
        .part(
            "image",
            new ByteArrayResource(rawImage) {
              @Override
              public String getFilename() {
                return "masking_input.png"; // FastAPI가 '파일'로 인식하려면 이름이 필요함
              }
            })
        .header("Content-Type", "image/png");

    try {
      byte[] responseImage =
          fastApiWebClient
              .post()
              .uri("/masking")
              .contentType(MediaType.MULTIPART_FORM_DATA)
              .body(BodyInserters.fromMultipartData(builder.build()))
              .retrieve()
              .bodyToMono(byte[].class)
              .block();

      return s3ImageService.upload(responseImage, "result.png");
    } catch (Exception e) {
      throw new RuntimeException("AI 서버와 통신 중 오류가 발생했습니다.");
    }
  }

  @Override
  public GetClosetResponse getClosetForAiRecommendation(Integer userId, Boolean onlyMine) {
    List<Clothes> savedClothes = clothesRepository.findSavedClothesByUserId(userId, onlyMine);

    List<ClosetClothesItem> top = new ArrayList<>();
    List<ClosetClothesItem> bottom = new ArrayList<>();
    List<ClosetClothesItem> accessories = new ArrayList<>();
    List<ClosetClothesItem> bags = new ArrayList<>();
    List<ClosetClothesItem> outer = new ArrayList<>();
    List<ClosetClothesItem> shoes = new ArrayList<>();

    for (Clothes c : savedClothes) {
      ClosetClothesItem item = ClosetClothesItem.from(c);

      switch (c.getClothesType()) {
        case TOP -> top.add(item);
        case BOTTOM -> bottom.add(item);
        case ACCESSORIES -> accessories.add(item);
        case BAG -> bags.add(item);
        case OUTER -> outer.add(item);
        case SHOES -> shoes.add(item);
      }
    }

    return new GetClosetResponse(top, bottom, accessories, bags, outer, shoes);
  }

  @Transactional(readOnly = true)
  @Override
  public List<ClothesRecommendItem> getClothesRecommend(Integer clothedId, Integer userId) {

    Clothes target =
        clothesRepository
            .findByIdAndDeletedAtIsNull(clothedId)
            .orElseThrow(() -> new NotFoundException("존재하지 않는 옷입니다."));

    if (!target.getUserId().equals(userId)) {
      return Collections.emptyList();
    }

    List<Integer> seasonIds = target.getSeasons().stream().map(Season::getId).distinct().toList();

    List<ClothesRecommendRow> rows =
        clothesRepository.recommendTopByCategory(userId, clothedId, seasonIds);

    return rows.stream()
        .map(r -> new ClothesRecommendItem(r.getClothesId(), r.getPhotoUrl()))
        .toList();
  }

  @Transactional
  @Override
  public void saveClothes(Integer clothesId, Integer userId) {
    SaveId saveId = new SaveId(clothesId, userId);

    if (saveRepository.existsById(saveId)) {
      throw new ConflictException("이미 저장한 옷입니다.");
    }

    User user =
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("존재하지 않는 유저입니다."));

    Clothes clothes =
        clothesRepository
            .findByIdAndDeletedAtIsNull(clothesId)
            .orElseThrow(() -> new NotFoundException("존재하지 않는 옷입니다."));

    if (clothes.getUserId() != null && clothes.getUserId().equals(userId)) {
      throw new ForbiddenException("자신의 옷은 저장할 수 없습니다.");
    }

    Save save =
        Save.builder()
            .id(saveId)
            .user(user)
            .clothes(clothes)
            .createdAt(LocalDateTime.now())
            .build();

    saveRepository.save(save);
  }

  @Transactional
  @Override
  public void unsaveClothes(Integer clothesId, Integer userId) {
    SaveId saveId = new SaveId(clothesId, userId);
    if (!saveRepository.existsById(saveId)) {
      throw new ConflictException("저장되지 않은 옷입니다.");
    }
    saveRepository.deleteById(saveId);
  }
}
