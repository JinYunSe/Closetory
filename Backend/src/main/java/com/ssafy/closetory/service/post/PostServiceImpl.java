package com.ssafy.closetory.service.post;

import com.ssafy.closetory.dto.post.PostCreateRequest;
import com.ssafy.closetory.dto.post.PostCreateResponse;
import com.ssafy.closetory.entity.post.Post;
import com.ssafy.closetory.entity.post.PostItemClothes;
import com.ssafy.closetory.exception.common.BadRequestException;
import com.ssafy.closetory.repository.PostItemClothesRepository;
import com.ssafy.closetory.repository.PostRepository;
import com.ssafy.closetory.service.s3.S3ImageService;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {
  private final PostRepository postRepository;
  private final PostItemClothesRepository postItemClothesRepository;
  private final S3ImageService s3ImageService;

  @Override
  public PostCreateResponse createPost(
      Integer userId, PostCreateRequest request, MultipartFile photo) {
    if (request == null) {
      throw new BadRequestException("잘못된 게시글 요청입니다.");
    }

    if (request.title() == null || request.title().isBlank()) {
      throw new BadRequestException("제목을 입력해주세요.");
    }

    if (request.content() == null || request.content().isBlank()) {
      throw new BadRequestException("내용을 입력해주세요.");
    }

    if (photo == null || photo.isEmpty()) {
      throw new BadRequestException("이미지는 필수입니다.");
    }
    String photoUrl = s3ImageService.upload(photo);

    // post 엔티티 생성 및 저장
    Post post =
        Post.builder()
            .title(request.title())
            .photoUrl(photoUrl)
            .content(request.content())
            .userId(userId)
            .views(0)
            .createdAt(LocalDateTime.now())
            .build();

    Post savedPost = postRepository.save(post);
    // 코디 아이템 매핑
    List<Integer> items = request.items();

    if (items != null && !items.isEmpty()) {
      List<PostItemClothes> mappings =
          items.stream()
              .map(
                  clothesId ->
                      PostItemClothes.builder()
                          .postId(savedPost.getId())
                          .clothesId(clothesId)
                          .build())
              .toList();
      postItemClothesRepository.saveAll(mappings);
    }
    return new PostCreateResponse(
        savedPost.getId(),
        savedPost.getTitle(),
        savedPost.getPhotoUrl(),
        savedPost.getContent(),
        items == null ? List.of() : items);
  }
}
