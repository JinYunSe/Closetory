package com.ssafy.closetory.service.user;

import com.ssafy.closetory.dto.user.AddStyleRequest;
import com.ssafy.closetory.dto.user.UpdateUserRequest;
import com.ssafy.closetory.dto.user.UserDetailResponse;
import com.ssafy.closetory.entity.clothes.Tag;
import com.ssafy.closetory.entity.user.User;
import com.ssafy.closetory.entity.user.UserFavoriteTag;
import com.ssafy.closetory.exception.common.*;
import com.ssafy.closetory.repository.TagRepository;
import com.ssafy.closetory.repository.UserFavoriteTagRepository;
import com.ssafy.closetory.repository.UserRepository;
import com.ssafy.closetory.service.s3.S3ImageService;
import com.ssafy.closetory.service.token.RefreshTokenService;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final TagRepository tagRepository;
  private final UserFavoriteTagRepository userFavoriteTagRepository;
  private final S3ImageService s3ImageService;
  private final PasswordEncoder passwordEncoder;
  private final RefreshTokenService refreshTokenService;

  // 비밀번호 검증
  @Override
  public void updateUser(
      Integer authUserId,
      Integer userId,
      UpdateUserRequest request,
      MultipartFile profilePhoto,
      MultipartFile bodyPhoto) {
    if (!authUserId.equals(userId)) {
      throw new ForbiddenException("사용자의 권한이 없습니다.");
    }

    User user =
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("존재하지 않는 사용자입니다."));

    if (profilePhoto != null && !profilePhoto.isEmpty()) {
      String profileUrl = s3ImageService.upload(profilePhoto);
      user.updateProfilePhotoUrl(profileUrl);
    }

    if (bodyPhoto != null && !bodyPhoto.isEmpty()) {
      String bodyUrl = s3ImageService.upload(bodyPhoto);
      user.updateBodyPhotoUrl(bodyUrl);
    }

    if (request != null) {
      if (request.nickname() != null) {
        user.updateNickname(request.nickname());
      }
      if (request.gender() != null) {
        user.updateGender(request.gender());
      }
      if (request.height() != null) {
        user.updateHeight(request.height().shortValue());
      }
      if (request.weight() != null) {
        user.updateWeight(request.weight().shortValue());
      }
      if (request.alarmEnabled() != null) {
        user.updateAlarmEnabled(request.alarmEnabled());
      }
    }
  }

  @Override
  @Transactional(readOnly = true)
  public void verifyPassword(Integer userId, String password) {
    User user = getUserWithValidation(userId);

    if (!passwordEncoder.matches(password, user.getPassword())) {
      throw new BadRequestException("비밀번호를 확인해주세요");
    }
  }

  // 비밀번호 변경
  @Override
  public void changePassword(Integer userId, String newPassword, String newPasswordConfirm) {
    User user = getUserWithValidation(userId);

    if (!newPassword.equals(newPasswordConfirm)) {
      throw new BadRequestException("비밀번호가 일치하지 않습니다");
    }

    user.changePassword(passwordEncoder.encode(newPassword));
  }

  @Transactional
  @Override
  public void addStyle(Integer userId, Integer authUserId, AddStyleRequest request) {

    userRepository.findById(userId).orElseThrow(() -> new NotFoundException("존재하지 않는 사용자입니다."));

    if (!userId.equals(authUserId)) {
      throw new ForbiddenException("자신의 선호 태그만 등록할 수 있습니다.");
    }

    List<Integer> tagIds =
        request.tags() == null ? List.of() : request.tags().stream().distinct().toList();

    if (tagIds.size() < 3) {
      throw new BadRequestException("선호 태그는 최소 3개 이상 선택해야 합니다.");
    }

    List<Tag> tags = tagRepository.findAllById(tagIds);
    if (tags.size() != tagIds.size()) {
      throw new NotFoundException("존재하지 않는 태그가 포함되어 있습니다.");
    }

    userFavoriteTagRepository.deleteByIdUserId(userId);

    List<UserFavoriteTag> rows =
        tagIds.stream().map(tagId -> UserFavoriteTag.of(userId, tagId)).toList();

    userFavoriteTagRepository.saveAll(rows);
  }

  // 사용자 검증
  private User getUserWithValidation(Integer pathUserId) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null || !authentication.isAuthenticated()) {
      throw new UnauthorizedException("인증되지 않은 사용자입니다");
    }

    Integer tokenUserId;
    try {
      tokenUserId = Integer.valueOf(authentication.getName());
    } catch (NumberFormatException e) {
      throw new UnauthorizedException("유효하지 않은 사용자입니다");
    }

    if (!tokenUserId.equals(pathUserId)) {
      throw new UnauthorizedException("유효하지 않은 사용자입니다");
    }

    return userRepository
        .findById(tokenUserId)
        .orElseThrow(() -> new UnauthorizedException("유효하지 않은 사용자입니다"));
  }

  @Override
  public UserDetailResponse getUserDetail(Integer authUserId, Integer userId) {

    if (!authUserId.equals(userId)) {
      throw new ForbiddenException("회원정보 조회 권한이 없습니다.");
    }

    User user =
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("존재하지 않는 사용자입니다."));

    return UserDetailResponse.from(user);
  }

  @Override
  public void deleteUser(Integer authUserId, Integer userId, String password) {
    if (!authUserId.equals(userId)) {
      throw new ForbiddenException("본인계정은 본인만 탈퇴할 수 있습니다.");
    }

    User user =
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("존재하지 않는 사용자입니다."));

    if (!passwordEncoder.matches(password, user.getPassword())) {
      throw new BadRequestException("비밀번호가 올바르지 않습니다.");
    }

    user.setDeletedAt(LocalDateTime.now());

    refreshTokenService.deleteByUserId(userId);
  }
}
