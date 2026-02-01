package com.ssafy.closetory.service.looks;

import com.ssafy.closetory.dto.clothes.ClosetClothesItem;
import com.ssafy.closetory.dto.clothes.GetClosetResponse;
import com.ssafy.closetory.dto.looks.*;
import com.ssafy.closetory.entity.clothes.Clothes;
import com.ssafy.closetory.entity.looks.ClothesLooks;
import com.ssafy.closetory.entity.looks.ClothesLooksId;
import com.ssafy.closetory.entity.looks.Look;
import com.ssafy.closetory.entity.post.Post;
import com.ssafy.closetory.entity.user.User;
import com.ssafy.closetory.enums.ClothesColor;
import com.ssafy.closetory.enums.ClothesType;
import com.ssafy.closetory.exception.common.BadRequestException;
import com.ssafy.closetory.exception.common.NotFoundException;
import com.ssafy.closetory.repository.*;
import com.ssafy.closetory.service.clothes.ClothesService;
import com.ssafy.closetory.service.s3.S3ImageService;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LookServiceImpl implements LookService {

  private final ClothesRepository clothesRepository;
  private final UserRepository userRepository;
  private final WebClient fastApiWebClient;
  private final S3ImageService s3ImageService;
  private final PostRepository postRepository;
  private final LookRepository lookRepository;
  private final ClothesService clothesService;
  private final ClothesLooksRepository clothesLooksRepository;

  private static final String[] clothesType = {
    "top_image", "bottom_image", "shoes_image",
    "outer_image", "accessory_image", "bag_image"
  };

  @Override
  public String requestFitting(Integer userId, VirtualFittingRequest request) {

    User user =
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("존재하지 않는 사용자입니다."));

    List<Integer> clothesIdList = request.clothesIdList();
    List<Integer> validIdList = clothesIdList.stream().filter(id -> id != -1).toList();

    List<Clothes> validClothes = clothesRepository.findAllById(validIdList);

    // 요청한 ID 개수와 찾은 옷 개수가 다르면 (없는 옷을 요청했다면) 예외 처리
    if (validClothes.size() != validIdList.size()) {
      throw new BadRequestException("존재하지 않는 옷 ID가 포함되어 있습니다.");
    }

    Map<Integer, Clothes> clothesMap =
        validClothes.stream().collect(Collectors.toMap(Clothes::getId, c -> c));

    MultipartBodyBuilder builder = new MultipartBodyBuilder();

    if (user.getBodyPhotoUrl() != null) {
      addToBuilder(builder, "model_image", user.getBodyPhotoUrl(), userId);
    } else {
      throw new BadRequestException("사용자 전신 사진이 필요합니다.");
    }

    for (int i = 0; i < clothesIdList.size(); i++) {
      if (i >= clothesType.length) {
        throw new BadRequestException("선택할 수 있는 개수를 초과했습니다.");
      }

      Integer id = clothesIdList.get(i);

      if (id == -1) {
        continue;
      }

      Clothes clothes = clothesMap.get(id);
      if (clothes != null) {
        String type = clothesType[i];
        addToBuilder(builder, type, clothes.getPhotoUrl(), clothes.getId());
      }
    }

    try {
      byte[] response =
          fastApiWebClient
              .post()
              .uri("/virtual-fitting")
              .contentType(MediaType.MULTIPART_FORM_DATA)
              .body(BodyInserters.fromMultipartData(builder.build()))
              .retrieve()
              .bodyToMono(byte[].class)
              .block();

      return s3ImageService.upload(response, "result.png");
    } catch (Exception e) {
      throw new RuntimeException("AI 서버와 통신 중 오류가 발생했습니다.");
    }
  }

  private void addToBuilder(
      MultipartBodyBuilder builder, String imageType, String url, Integer idForName) {
    try {
      byte[] imageByte = downloadImageFromUrl(url);

      int lastDotIndex = url.lastIndexOf(".");
      if (lastDotIndex == -1) {
        throw new BadRequestException("이미지 URL에 확장자가 없습니다.: " + url);
      }

      String extension = url.substring(lastDotIndex);
      String filename = imageType + "_" + idForName + extension;

      ByteArrayResource resource = createNamedResource(imageByte, filename);

      builder.part(imageType, resource);
    } catch (IOException e) {
      throw new RuntimeException("이미지 다운로드 중 오류 발생: " + url, e);
    }
  }

  private byte[] downloadImageFromUrl(String fileUrl) throws IOException {
    URL url = new URL(fileUrl);

    try (InputStream in = url.openStream()) {
      return in.readAllBytes();
    }
  }

  private ByteArrayResource createNamedResource(byte[] content, String filename) {
    return new ByteArrayResource(content) {

      @Override
      public String getFilename() {
        return filename;
      }
    };
  }

  @Override
  public AiRecommendationResponse requestAiRecommendation(
      Integer userId, AiRecommendationRequest request) {

    Pageable limit = PageRequest.of(0, 10);
    List<Post> posts;

    // true, false에 따라 참고하는 게시글이 달라짐
    // true : 내가 원하는 코디, false : 나에게 어울리는 코디
    if (request.isPersonalized() == true) {
      posts = postRepository.findLikedPostsByUserId(userId, limit);
    } else {
      posts = postRepository.findWrittenPostsByUserId(userId, limit);
    }

    // error 발생
    // '내가 원하는 코디'의 경우, 좋아요를 누른 게시글이 없는 경우
    // '나에게 어울리는 코디'의 경우, 내가 작성한 게시글이 없는 경우
    if (posts.isEmpty()) {
      posts = postRepository.findPostsByViews(limit);
      if (posts.isEmpty()) {
        throw new NotFoundException("추천할 게시글이 존재하지 않습니다.");
      }
    }

    // 상위 10개의 게시글 중, 랜덤하게 선택
    // 상위 5개는 동일하게 15%, 하위 5개는 동일 X
    Random random = new Random();
    int percent = random.nextInt(100);

    int randomIndex;
    if (percent < 75) {
      randomIndex = percent / 15;
    } else if (percent < 82) {
      randomIndex = 5;
    } else if (percent < 88) {
      randomIndex = 6;
    } else if (percent < 93) {
      randomIndex = 7;
    } else if (percent < 97) {
      randomIndex = 8;
    } else {
      randomIndex = 9;
    }

    if (randomIndex >= posts.size()) {
      randomIndex = 0;
    }

    GetClosetResponse myCloset =
        clothesService.getClosetForAiRecommendation(userId, request.onlyMine());

    FastApiRequest aiRequest =
        FastApiRequest.builder()
            .refImage(posts.get(randomIndex).getPhotoUrl())
            .tops(toUrlList(myCloset.topClothes()))
            .bottoms(toUrlList(myCloset.bottomClothes()))
            .shoes(toUrlList(myCloset.shoes()))
            .outers(toUrlList(myCloset.outerClothes()))
            .accessories(toUrlList(myCloset.accessories()))
            .bags(toUrlList(myCloset.bags()))
            .build();

    if (aiRequest.tops().isEmpty()) {
      throw new BadRequestException("상의를 추가해주세요.");
    } else if (aiRequest.bottoms().isEmpty()) {
      throw new BadRequestException("하의를 추가해주세요.");
    } else if (aiRequest.shoes().isEmpty()) {
      throw new BadRequestException("신발을 추가해주세요.");
    }

    FastApiResponse aiResponse =
        fastApiWebClient
            .post()
            .uri("/recommend-fit")
            .bodyValue(aiRequest)
            .retrieve()
            .bodyToMono(FastApiResponse.class)
            .block();

    if (aiResponse == null) {
      throw new RuntimeException();
    }

    List<LooksItem> selectedItems = new ArrayList<>();
    Map<String, Integer> selections = aiResponse.selections();

    selectedLookItem(selectedItems, selections.get("Top"), myCloset.topClothes(), ClothesType.TOP);
    selectedLookItem(
        selectedItems, selections.get("Bottom"), myCloset.bottomClothes(), ClothesType.BOTTOM);
    selectedLookItem(selectedItems, selections.get("Shoes"), myCloset.shoes(), ClothesType.SHOES);
    selectedLookItem(
        selectedItems, selections.get("Outer"), myCloset.outerClothes(), ClothesType.OUTER);
    selectedLookItem(selectedItems, selections.get("Bag"), myCloset.bags(), ClothesType.BAG);
    selectedLookItem(
        selectedItems,
        selections.get("Accessory"),
        myCloset.accessories(),
        ClothesType.ACCESSORIES);

    // 6. 결과 반환
    return new AiRecommendationResponse(aiResponse.reason(), selectedItems);
  }

  private List<String> toUrlList(List<ClosetClothesItem> items) {
    if (items == null) return new ArrayList<>();

    return items.stream().map(ClosetClothesItem::photoUrl).toList();
  }

  private void selectedLookItem(
      List<LooksItem> resultList,
      Integer index,
      List<ClosetClothesItem> sourceList,
      ClothesType type) {

    if (index != null && index >= 0 && index < sourceList.size()) {
      // 1. 리스트에서 꺼냄 (ID, URL 있음)
      ClosetClothesItem item = sourceList.get(index);

      // 2. LooksItem 생성
      resultList.add(
          new LooksItem(
              item.clothesId(),
              type, // 여기서 전달받은 타입을 사용
              item.photoUrl()));
    }
  }

  @Override
  @Transactional
  public void lookRegistration(LookRegistrationRequest request, Integer userId) {
    Look look =
        Look.builder()
            .userId(userId)
            .photoUrl(request.aiImageUrl())
            .reason(request.aiReason())
            .build();

    Look saved = lookRepository.save(look);

    List<Clothes> clothesList = clothesRepository.findAllById(request.clothesIdList());

    if (clothesList.size() != request.clothesIdList().size()) {
      throw new BadRequestException("존재하지 않는 옷 ID가 포함되어 있습니다.");
    }

    List<ClothesLooks> clothesLooksList = new ArrayList<>();

    for (Clothes c : clothesList) {
      ClothesLooksId id = new ClothesLooksId(c.getId(), saved.getId());
      ClothesLooks clothesLooks = ClothesLooks.builder().id(id).clothes(c).look(saved).build();

      clothesLooksList.add(clothesLooks);
    }

    clothesLooksRepository.saveAll(clothesLooksList);
  }

  @Override
  public List<GetAllLooksResponse> getAllLooks(Integer userId) {
    List<Look> lookList = lookRepository.findAllByUserIdOrderByCreatedAtDesc(userId);

    if (lookList.isEmpty()) {
      return new ArrayList<>();
    }

    List<Integer> lookIds = lookList.stream().map(Look::getId).toList();

    List<ClothesLooks> allLookItems = clothesLooksRepository.findByIdLookIdIn(lookIds);

    Map<Integer, List<ClothesLooks>> clothesMap =
        allLookItems.stream().collect(Collectors.groupingBy(cl -> cl.getLook().getId()));

    List<GetAllLooksResponse> result = new ArrayList<>();

    for (Look look : lookList) {
      List<ClothesLooks> items = clothesMap.get(look.getId());
      boolean onlyMine = true;

      for (ClothesLooks item : items) {
        if (!item.getClothes().getUserId().equals(userId)) {
          onlyMine = false;
          break;
        }
      }

      GetAllLooksResponse lookResponse =
          GetAllLooksResponse.builder()
              .lookId(look.getId())
              .photoUrl(look.getPhotoUrl())
              .date(look.getDate())
              .aiReason(look.getReason())
              .onlyMine(onlyMine)
              .build();

      result.add(lookResponse);
    }

    return result;
  }

  @Override
  public List<GetLooksByMonthResponse> getLooksByMonthResponse(boolean isMain, Integer userId) {
    LocalDate startDate = LocalDate.now();
    LocalDate endDate = startDate.plusMonths(2).with(TemporalAdjusters.lastDayOfMonth());

    List<Look> looksByMonth =
        lookRepository.findAllByUserIdAndDateBetweenOrderByDateAsc(userId, startDate, endDate);

    if (looksByMonth.isEmpty()) {
      return new ArrayList<>();
    }

    List<Integer> lookIds = looksByMonth.stream().map(Look::getId).toList();

    List<ClothesType> targetTypes = List.of(ClothesType.TOP, ClothesType.BOTTOM);
    List<ClothesLooks> allLookItems =
        clothesLooksRepository.findTopBottomByIdLookIdIn(lookIds, targetTypes);

    Map<Integer, List<ClothesLooks>> clothesMap =
        allLookItems.stream().collect(Collectors.groupingBy(cl -> cl.getLook().getId()));

    List<GetLooksByMonthResponse> result = new ArrayList<>();

    for (Look look : looksByMonth) {
      var builder = GetLooksByMonthResponse.builder().lookId(look.getId()).date(look.getDate());

      if (isMain) {
        List<ClothesLooks> items = clothesMap.getOrDefault(look.getId(), Collections.emptyList());

        ClothesColor topColor = findColorByType(items, ClothesType.TOP);
        ClothesColor bottomColor = findColorByType(items, ClothesType.BOTTOM);

        builder.photoUrl(look.getPhotoUrl()).topColor(topColor).bottomColor(bottomColor);
      }

      result.add(builder.build());
    }

    return result;
  }

  private ClothesColor findColorByType(List<ClothesLooks> items, ClothesType type) {
    return items.stream()
        .map(ClothesLooks::getClothes)
        .filter(c -> ((Clothes) c).getClothesType() == type)
        .findFirst()
        .map(Clothes::getColor)
        .orElse(null);
  }
}
