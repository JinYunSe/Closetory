package com.ssafy.closetory.service.clothes;

import com.ssafy.closetory.dto.clothes.*;
import com.ssafy.closetory.entity.clothes.Clothes;
import com.ssafy.closetory.entity.clothes.Season;
import com.ssafy.closetory.entity.clothes.Tag;
import com.ssafy.closetory.enums.ClothesColor;
import com.ssafy.closetory.exception.common.BadRequestException;
import com.ssafy.closetory.exception.common.ForbiddenException;
import com.ssafy.closetory.exception.common.NotFoundException;
import com.ssafy.closetory.repository.*;
import com.ssafy.closetory.service.s3.S3ImageService;
import java.time.LocalDateTime;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClothesServiceImpl implements ClothesService {

  private final ClothesRepository clothesRepository;
  private final TagRepository tagRepository;
  private final SeasonRepository seasonRepository;
  private final S3ImageService s3ImageService;
  private final WebClient fastApiWebClient;
  private final RestClient.Builder builder;

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
  public void addClothes(Integer userId, AddClothesRequest request, MultipartFile photo) {
    String photoUrl;
    photoUrl = s3ImageService.upload(photo);

    Clothes clothes =
        Clothes.builder()
            .photoUrl(photoUrl)
            .clothesType(request.clothesType())
            .color(request.color())
            .userId(userId)
            .createdAt(LocalDateTime.now())
            .build();

    if (request.tags() != null && !request.tags().isEmpty()) {
      List<Tag> tags = tagRepository.findAllById(request.tags());
      if (tags.size() != request.tags().size()) {
        throw new NotFoundException("존재하지 않는 태그가 포함되어 있습니다.");
      }
      clothes.getTags().addAll(tags);
    }

    if (request.seasons() != null && !request.seasons().isEmpty()) {
      List<Season> seasons = seasonRepository.findAllById(request.seasons());
      if (seasons.size() != request.seasons().size()) {
        throw new NotFoundException("존재하지 않는 계절이 포함되어 있습니다.");
      }
      clothes.getSeasons().addAll(seasons);
    }

    clothesRepository.save(clothes);
  }

  @Transactional
  @Override
  public GetClothesDetailResponse updateClothes(
      Integer userId, Integer clothesId, UpdateClothesRequest req, MultipartFile photo) {

    Clothes clothes =
        clothesRepository
            .findByIdAndDeletedAtIsNull(clothesId)
            .orElseThrow(() -> new NotFoundException("존재하지 않는 옷입니다."));

    if (!clothes.getUserId().equals(userId)) {
      throw new ForbiddenException("자신의 옷만 수정할 수 있습니다.");
    }

    if (req.clothesType() != null) clothes.setClothesType(req.clothesType());
    if (req.color() != null) clothes.setColor(req.color());
    if (req.tags() != null) {
      Set<Tag> newTags = new HashSet<>(tagRepository.findAllById(req.tags()));
      if (newTags.size() != req.tags().size()) {
        throw new BadRequestException("존재하지 않는 태그가 포함되어 있습니다.");
      }
      clothes.getTags().clear();
      clothes.getTags().addAll(newTags);
    }

    if (req.seasons() != null) {
      Set<Season> newSeasons = new HashSet<>(seasonRepository.findAllById(req.seasons()));
      if (newSeasons.size() != req.seasons().size()) {
        throw new BadRequestException("존재하지 않는 계절이 포함되어 있습니다.");
      }
      clothes.getSeasons().clear();
      clothes.getSeasons().addAll(newSeasons);
    }

    if (photo != null && !photo.isEmpty()) {
      String newUrl = s3ImageService.upload(photo);
      clothes.setPhotoUrl(newUrl);
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
}
