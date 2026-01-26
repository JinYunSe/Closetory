package com.ssafy.closetory.service.user;

import com.ssafy.closetory.entity.user.User;
import com.ssafy.closetory.exception.common.BadRequestException;
import com.ssafy.closetory.exception.common.UnauthorizedException;
import com.ssafy.closetory.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  // 비밀번호 검증

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
}
