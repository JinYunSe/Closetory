package com.ssafy.closetory.service.post;

import com.ssafy.closetory.dto.post.PostCreateRequest;
import com.ssafy.closetory.dto.post.PostCreateResponse;
import com.ssafy.closetory.entity.post.Post;
import com.ssafy.closetory.entity.post.PostItemClothes;
import com.ssafy.closetory.exception.common.BadRequestException;
import com.ssafy.closetory.repository.PostItemClothesRepository;
import com.ssafy.closetory.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {
  private final PostRepository postRepository;
  private final PostItemClothesRepository postItemClothesRepository;

  @Override
  public PostCreateResponse createPost(Integer userId, PostCreateRequest request) {
    if (request == null) {
      throw new BadRequestException("잘못된 게시글 요청입니다.");
    }

    if (request.title() == null || request.title().isBlank()) {
      throw new BadRequestException("잘못된 게시글 요청입니다.");
    }

    if (request.content() == null || request.content().isBlank()) {
      throw new BadRequestException("잘못된 게시글 요청입니다.");
    }

    // post 엔티티 생성 및 저장
    Post post =
        Post.builder()
            .title(request.title())
            .photoUrl(request.photoUrl())
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
