package com.ssafy.closetory.service.post;

import com.ssafy.closetory.dto.post.PostCreateRequest;
import com.ssafy.closetory.dto.post.PostCreateResponse;

public interface PostService {
  PostCreateResponse createPost(Integer userId, PostCreateRequest request);
}
