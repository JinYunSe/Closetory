package com.ssafy.closetory.controller.post;

import com.ssafy.closetory.dto.common.ApiResponse;
import com.ssafy.closetory.dto.post.PostCreateRequest;
import com.ssafy.closetory.dto.post.PostCreateResponse;
import com.ssafy.closetory.service.post.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
public class PostController {
  private final PostService postService;

  @PostMapping()
  @Operation(summary = "게시글 등록")
  @SecurityRequirement(name = "bearerAuth")
  ResponseEntity<ApiResponse<PostCreateResponse>> createPost(
      @AuthenticationPrincipal Integer userId, @Valid @RequestBody PostCreateRequest request) {
    PostCreateResponse response = postService.createPost(userId, request);

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.ok(201, "게시글 등록 완료", response));
  }
}
