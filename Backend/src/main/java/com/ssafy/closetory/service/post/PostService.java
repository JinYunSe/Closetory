package com.ssafy.closetory.service.post;

import com.ssafy.closetory.dto.post.*;
import com.ssafy.closetory.enums.SearchFilter;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface PostService {
  PostCreateResponse createPost(Integer userId, PostCreateRequest request, MultipartFile photo);

  PostCreateResponse updatePost(
      Integer userId, Integer postId, PostUpdateRequest request, MultipartFile photo);

  PostDetailResponse getPostDetail(Integer postId, Integer userId);

  void deletePost(Integer userId, Integer postId);

  void createLikes(Integer postId, Integer userId);

  void deleteLikes(Integer postId, Integer userId);

  List<PostSearchResponse> searchPosts(Integer userId, String keyword, SearchFilter filter);

  CreateCommentResponse createComment(Integer postId, CommentRequest request, Integer userId);

  UpdateCommentResponse updateComment(Integer postId, Integer commentId, CommentRequest request, Integer userId);
}
