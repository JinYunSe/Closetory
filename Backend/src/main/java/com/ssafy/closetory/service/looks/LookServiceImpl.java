package com.ssafy.closetory.service.looks;

import com.ssafy.closetory.dto.looks.VirtualFittingRequest;
import com.ssafy.closetory.entity.clothes.Clothes;
import com.ssafy.closetory.entity.user.User;
import com.ssafy.closetory.exception.common.BadRequestException;
import com.ssafy.closetory.exception.common.NotFoundException;
import com.ssafy.closetory.repository.ClothesRepository;
import com.ssafy.closetory.repository.UserRepository;
import com.ssafy.closetory.service.s3.S3ImageService;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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
public class LookServiceImpl implements LookService {

  private final ClothesRepository clothesRepository;
  private final UserRepository userRepository;
  private final WebClient fastApiWebClient;
  private final S3ImageService s3ImageService;

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
}
