package com.ssafy.closetory.controller.post;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.closetory.dto.common.ApiResponse;
import com.ssafy.closetory.dto.post.*;
import com.ssafy.closetory.enums.SearchFilter;
import com.ssafy.closetory.service.post.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
public class PostController {
  private final PostService postService;
  private final ObjectMapper objectMapper;

  @PostMapping(
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "게시글 등록")
  @SecurityRequirement(name = "bearerAuth")
  public ResponseEntity<ApiResponse<PostCreateResponse>> createPost(
      @RequestPart(value = "photo") MultipartFile photo,
      @RequestPart("request") String requestJson,
      @AuthenticationPrincipal Integer userId)
      throws JsonProcessingException {
    PostCreateRequest request = objectMapper.readValue(requestJson, PostCreateRequest.class);
    PostCreateResponse response = postService.createPost(userId, request, photo);

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.ok(201, "게시글 등록 완료", response));
  }

  @GetMapping("/{postId}")
  @Operation(summary = "게시글 상세 조회")
  @SecurityRequirement(name = "bearerAuth")
  public ResponseEntity<ApiResponse<PostDetailResponse>> getPostDetail(
      @PathVariable Integer postId, @AuthenticationPrincipal Integer userId) {

    PostDetailResponse response = postService.getPostDetail(postId, userId);

    return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(200, "게시글 수정 완료", response));
  }

  @PatchMapping(
      value = "/{postId}",
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "게시글 수정")
  @SecurityRequirement(name = "bearerAuth")
  public ResponseEntity<ApiResponse<PostCreateResponse>> updatePost(
      @PathVariable Integer postId,
      @RequestPart(value = "photo") MultipartFile photo,
      @RequestPart("request") String requestJson,
      @AuthenticationPrincipal Integer userId)
      throws JsonProcessingException {
    PostUpdateRequest request = objectMapper.readValue(requestJson, PostUpdateRequest.class);

    PostCreateResponse response = postService.updatePost(userId, postId, request, photo);

    return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(200, "게시글 수정 완료", response));
  }

  @DeleteMapping("/{postId}")
  @Operation(summary = "게시글 삭제")
  @SecurityRequirement(name = "bearerAuth")
  public ResponseEntity<ApiResponse<Void>> deletePost(
      @AuthenticationPrincipal Integer userId, @PathVariable Integer postId) {
    postService.deletePost(userId, postId);

    return ResponseEntity.status(HttpStatus.OK)
        .body(ApiResponse.ok(200, "게시글이 성공적으로 삭제되었습니다", null));
  }

  @PostMapping("/{postId}/like")
  @Operation(summary = "좋아요 생성")
  @SecurityRequirement(name = "bearerAuth")
  public ResponseEntity<ApiResponse<Void>> createLikes(
      @PathVariable Integer postId, @AuthenticationPrincipal Integer userId) {
    postService.createLikes(postId, userId);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.ok(201, "게시글 좋아요 생성 완료", null));
  }

  @DeleteMapping("/{postId}/like")
  @Operation(summary = "좋아요 취소")
  @SecurityRequirement(name = "bearerAuth")
  public ResponseEntity<ApiResponse<Void>> deleteLikes(
      @PathVariable Integer postId, @AuthenticationPrincipal Integer userId) {
    postService.deleteLikes(postId, userId);
    return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(200, "게시글 좋아요 취소 완료", null));
  }

  @GetMapping()
  @Operation(summary = "게시글 검색 목록 조회")
  @SecurityRequirement(name = "bearerAuth")
  public ResponseEntity<ApiResponse<List<PostSearchResponse>>> searchPosts(
      @AuthenticationPrincipal Integer userId,
      @RequestParam(required = false) String keyword,
      @RequestParam(defaultValue = "LATEST") SearchFilter searchfilter) {
    return ResponseEntity.status(HttpStatus.OK)
        .body(
            ApiResponse.ok(
                200, "게시글 검색 결과 조회 성공", postService.searchPosts(userId, keyword, searchfilter)));
  }
}
