package com.ssafy.closetory.service.post;

import com.ssafy.closetory.dto.post.*;
import com.ssafy.closetory.entity.post.Comment;
import com.ssafy.closetory.entity.post.Likes;
import com.ssafy.closetory.entity.post.LikesId;
import com.ssafy.closetory.entity.post.Post;
import com.ssafy.closetory.entity.user.User;
import com.ssafy.closetory.enums.SearchFilter;
import com.ssafy.closetory.exception.common.BadRequestException;
import com.ssafy.closetory.exception.common.ConflictException;
import com.ssafy.closetory.exception.common.ForbiddenException;
import com.ssafy.closetory.exception.common.NotFoundException;
import com.ssafy.closetory.repository.*;
import com.ssafy.closetory.service.s3.S3ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {
  private final PostRepository postRepository;
  private final ClothesRepository clothesRepository;
  private final S3ImageService s3ImageService;
  private final SaveRepository saveRepository;
  private final UserRepository userRepository;
  private final LikesRepository likesRepository;
  private final CommentRepository commentRepository;

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
  @Transactional
  public PostCreateResponse updatePost(
      Integer userId, Integer postId, PostUpdateRequest request, MultipartFile photo) {

    Post post =
        postRepository.findById(postId).orElseThrow(() -> new NotFoundException("존재하지 않는 게시글입니다."));

    if (!post.getUserId().equals(userId)) {
      throw new ForbiddenException("게시글 수정 권한이 없습니다.");
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
  @Transactional(readOnly = true)
  public PostDetailResponse getPostDetail(Integer postId, Integer userId) {

    Post post =
        postRepository
            .findById(postId)
            .orElseThrow(() -> new BadRequestException("존재하지 않는 게시글입니다."));

    User user =
        userRepository
            .findById(post.getUserId())
            .orElseThrow(() -> new BadRequestException("작성자 정보가 존재하지 않습니다."));

    Integer likeCount = likesRepository.countByPostId(postId);
    boolean isLiked = likesRepository.existsByUserIdAndPostId(userId, postId);

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
        likeCount,
        isLiked,
        user.getId(),
        user.getNickname(),
        user.getProfilePhotoUrl());
  }

  @Override
  @Transactional
  public void increasePostViews(Integer postId) {
    postRepository.increaseViews(postId);
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

  @Override
  public void createLikes(Integer postId, Integer userId) {
    Post post =
        postRepository
            .findById(postId)
            .orElseThrow(() -> new BadRequestException("없는 게시판 번호입니다. 다시 확인해주세요!"));

    User user =
        userRepository.findById(userId).orElseThrow(() -> new BadRequestException("회원가입이 필요합니다!"));

    LikesId id = new LikesId(userId, postId);

    if (likesRepository.existsById(id)) {
      throw new ConflictException("이미 좋아요를 누른 게시글입니다.");
    }

    Likes likeInstance = Likes.builder().id(id).user(user).post(post).build();

    likesRepository.save(likeInstance);
  }

  @Override
  public void deleteLikes(Integer postId, Integer userId) {
    LikesId id = new LikesId(userId, postId);

    if (!likesRepository.existsById(id)) {
      throw new BadRequestException("좋아요를 누른 적이 없는 게시글입니다.");
    }

    likesRepository.deleteById(id);
  }

  @Override
  public List<PostSearchResponse> searchPosts(Integer userId, String keyword, SearchFilter filter) {
    return switch (filter) {
      case LIKED -> searchLikedPosts(userId, keyword);
      case WRITTEN -> searchWrittenPosts(userId, keyword);
      case POPULAR -> searchPopularPosts(keyword);
      case LATEST -> searchLatestPosts(keyword);
    };
  }

  private List<PostSearchResponse> searchLatestPosts(String keyword) {
    List<Post> posts = postRepository.findLatestPosts(keyword);

    return posts.stream()
        .map(
            post ->
                PostSearchResponse.builder()
                    .postId(post.getId())
                    .title(post.getTitle())
                    .photoUrl(post.getPhotoUrl())
                    .views(post.getViews())
                    .likes(likesRepository.countByPostId(post.getId()))
                    .comments(commentRepository.countByPostId(post.getId()))
                    .build())
        .toList();
  }

  private List<PostSearchResponse> searchLikedPosts(Integer userId, String keyword) {
    List<Post> posts = postRepository.findLikedPosts(userId, keyword);

    return posts.stream()
        .map(
            post ->
                PostSearchResponse.builder()
                    .postId(post.getId())
                    .title(post.getTitle())
                    .photoUrl(post.getPhotoUrl())
                    .views(post.getViews())
                    .likes(likesRepository.countByPostId(post.getId()))
                    .comments(commentRepository.countByPostId(post.getId()))
                    .build())
        .toList();
  }

  private List<PostSearchResponse> searchWrittenPosts(Integer userId, String keyword) {
    List<Post> posts = postRepository.findWrittenPosts(userId, keyword);

    return posts.stream()
        .map(
            post ->
                PostSearchResponse.builder()
                    .postId(post.getId())
                    .title(post.getTitle())
                    .photoUrl(post.getPhotoUrl())
                    .views(post.getViews())
                    .likes(likesRepository.countByPostId(post.getId()))
                    .comments(commentRepository.countByPostId(post.getId()))
                    .build())
        .toList();
  }

  private List<PostSearchResponse> searchPopularPosts(String keyword) {
    List<Post> posts = postRepository.findPopularPosts(keyword);

    return posts.stream()
        .map(
            post ->
                PostSearchResponse.builder()
                    .postId(post.getId())
                    .title(post.getTitle())
                    .photoUrl(post.getPhotoUrl())
                    .views(post.getViews())
                    .likes(likesRepository.countByPostId(post.getId()))
                    .comments(commentRepository.countByPostId(post.getId()))
                    .build())
        .toList();
  }

  @Override
  public CreateCommentResponse createComment(
      Integer postId, CommentRequest request, Integer userId) {
    Post post =
        postRepository.findById(postId).orElseThrow(() -> new NotFoundException("존재하지 않는 게시글입니다."));

    User user =
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("회원가입이 필요합니다!"));

    Comment newComment = Comment.builder().content(request.content()).post(post).user(user).build();

    Comment saved = commentRepository.save(newComment);

    return CreateCommentResponse.builder()
        .commentId(saved.getId())
        .content(saved.getContent())
        .createdAt(saved.getCreatedAt())
        .build();
  }

  @Override
  public UpdateCommentResponse updateComment(
      Integer postId, Integer commentId, CommentRequest request, Integer userId) {
    Comment comment = findComment(postId, commentId, userId);

    comment.setContent(request.content());
    return new UpdateCommentResponse(comment.getId(), comment.getContent());
  }

  @Override
  public void deleteComment(Integer postId, Integer commentId, Integer userId) {
    Comment comment = findComment(postId, commentId, userId);

    comment.setDeletedAt(LocalDateTime.now());
  }

  private Comment findComment(Integer postId, Integer commentId, Integer userId) {
    Comment targetComment =
        commentRepository
            .findById(commentId)
            .orElseThrow(() -> new NotFoundException("댓글을 찾을 수 없습니다."));

    if (!targetComment.getPost().getId().equals(postId)) {
      throw new BadRequestException("댓글을 찾을 수 없습니다.");
    } else if (!targetComment.getUser().getId().equals(userId)) {
      throw new ForbiddenException("해당 댓글에 대한 권한이 없습니다.");
    }

    return targetComment;
  }

  @Override
  public List<GetAllCommentsResponse> getAllComments(Integer postId) {
    List<Comment> allComments =
        commentRepository.findAllByPostIdAndDeletedAtIsNullOrderByCreatedAtAsc(postId);

    List<GetAllCommentsResponse> result = new ArrayList<>();

    for (Comment comment : allComments) {
      GetAllCommentsResponse commentInstance =
          GetAllCommentsResponse.builder()
              .commentId(comment.getId())
              .nickname(comment.getUser().getNickname())
              .content(comment.getContent())
              .profileImage(comment.getUser().getProfilePhotoUrl())
              .createdAt(comment.getCreatedAt())
              .build();

      result.add(commentInstance);
    }

    return result;
  }
}
