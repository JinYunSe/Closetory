package com.ssafy.closetory.service.post;

import com.ssafy.closetory.dto.post.PostCreateRequest;
import com.ssafy.closetory.dto.post.PostCreateResponse;
import org.springframework.web.multipart.MultipartFile;

public interface PostService {
  PostCreateResponse createPost(Integer userId, PostCreateRequest request, MultipartFile photo);
}
