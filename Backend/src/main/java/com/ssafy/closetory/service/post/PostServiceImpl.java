package com.ssafy.closetory.service.post;

import com.ssafy.closetory.dto.post.*;
import com.ssafy.closetory.entity.post.Post;
import com.ssafy.closetory.entity.user.User;
import com.ssafy.closetory.exception.common.BadRequestException;
import com.ssafy.closetory.exception.common.ConflictException;
import com.ssafy.closetory.exception.common.ForbiddenException;
import com.ssafy.closetory.exception.common.NotFoundException;
import com.ssafy.closetory.repository.ClothesRepository;
import com.ssafy.closetory.repository.PostRepository;
import com.ssafy.closetory.repository.SaveRepository;
import com.ssafy.closetory.repository.UserRepository;
import com.ssafy.closetory.service.s3.S3ImageService;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional
public class PostServiceImpl implements PostService {
  private final PostRepository postRepository;
  private final ClothesRepository clothesRepository;
  private final S3ImageService s3ImageService;
  private final SaveRepository saveRepository;
  private final UserRepository userRepository;

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

    List<Integer> items = request.items();
    if (items != null && !items.isEmpty()) {
      for (Integer clothesId : items) {
        clothesRepository
            .findByIdAndDeletedAtIsNull(clothesId)
            .ifPresentOrElse(
                post.getClothes()::add,
                () -> {
                  throw new NotFoundException("존재하지 않은 옷이 포함되어 있습니다.");
                });
      }
    }

    Post savedPost = postRepository.save(post);
    return new PostCreateResponse(
        savedPost.getId(),
        savedPost.getTitle(),
        savedPost.getPhotoUrl(),
        savedPost.getContent(),
        items == null ? List.of() : items);
  }

  // 게시글 수정
  @Override
  public PostCreateResponse updatePost(
      Integer userId, Integer postId, PostUpdateRequest request, MultipartFile photo) {

    Post post =
        postRepository
            .findById(postId)
            .orElseThrow(() -> new BadRequestException("존재하지 않는 게시글입니다."));

    if (!post.getUserId().equals(userId)) {
      throw new BadRequestException("게시글 수정 권한이 없습니다.");
    }

    if (request.title() == null || request.title().isBlank()) {
      throw new BadRequestException("제목을 입력해주세요.");
    }

    if (request.content() == null || request.content().isBlank()) {
      throw new BadRequestException("내용을 입력해주세요.");
    }

    if (photo != null && !photo.isEmpty()) {
      post.updatePhoto(s3ImageService.upload(photo));
    }

    post.update(request.title(), request.content());

    post.getClothes().clear();

    List<Integer> items = request.items();
    if (items != null && !items.isEmpty()) {
      for (Integer clothesId : items) {
        clothesRepository
            .findByIdAndDeletedAtIsNull(clothesId)
            .ifPresentOrElse(
                post.getClothes()::add,
                () -> {
                  throw new NotFoundException("존재하지 않은 옷이 포함되어 있습니다.");
                });
      }
    }
    return new PostCreateResponse(
        post.getId(),
        post.getTitle(),
        post.getPhotoUrl(),
        post.getContent(),
        items == null ? List.of() : items);
  }

  @Override
  public PostDetailResponse getPostDetail(Integer postId, Integer userId) {

    Post post =
        postRepository
            .findById(postId)
            .orElseThrow(() -> new BadRequestException("존재하지 않는 게시글입니다."));

    User user =
        userRepository
            .findById(post.getUserId())
            .orElseThrow(() -> new BadRequestException("작성자 정보가 존재하지 않습니다."));

    post.increaseViews();

    List<PostItemResponse> items =
        post.getClothes().stream()
            .map(
                clothes -> {
                  Integer clothesId = clothes.getId();

                  boolean isSaved = saveRepository.existsByUserIdAndClothesId(userId, clothesId);

                  return new PostItemResponse(clothesId, clothes.getPhotoUrl(), isSaved);
                })
            .toList();
    return new PostDetailResponse(
        post.getId(),
        post.getTitle(),
        post.getPhotoUrl(),
        post.getContent(),
        items,
        post.getCreatedAt(),
        post.getViews(),
        0, // 좋아요 개수
        false, // 좋아요 유무
        user.getId(),
        user.getNickname(),
        user.getProfilePhotoUrl());
  }

  @Override
  @Transactional
  public void deletePost(Integer userId, Integer postId) {

    Post post =
        postRepository.findById(postId).orElseThrow(() -> new NotFoundException("존재하지 않는 게시글입니다."));

    if (!post.getUserId().equals(userId)) {
      throw new ForbiddenException("해당 게시글에 대한 권한이 없습니다.");
    }

    if (post.getDeletedAt() != null) {
      throw new ConflictException("이미 삭제된 게시글입니다.");
    }

    post.setDeletedAt(LocalDateTime.now());
  }
}
