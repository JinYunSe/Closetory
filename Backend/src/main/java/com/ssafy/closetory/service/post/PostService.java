package com.ssafy.closetory.service.post;

import com.ssafy.closetory.dto.post.PostCreateRequest;
import com.ssafy.closetory.dto.post.PostCreateResponse;
import com.ssafy.closetory.dto.post.PostDetailResponse;
import com.ssafy.closetory.dto.post.PostUpdateRequest;
import org.springframework.web.multipart.MultipartFile;

public interface PostService {
  PostCreateResponse createPost(Integer userId, PostCreateRequest request, MultipartFile photo);

  PostCreateResponse updatePost(
      Integer userId, Integer postId, PostUpdateRequest request, MultipartFile photo);

  PostDetailResponse getPostDetail(Integer postId, Integer userId);

  void createLikes(Integer postId, Integer userId);
}
