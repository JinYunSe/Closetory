package com.ssafy.closetory.service.auth;

import com.ssafy.closetory.dto.user.SignupRequest;
import com.ssafy.closetory.entity.user.User;
import com.ssafy.closetory.enums.Provider;
import com.ssafy.closetory.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  // 회원가입 기능 구현
  public void signup(SignupRequest request) {

    // 1. 비밀번호 불일치할 때 → 400
    if (!request.getPassword().equals(request.getPasswordConfirm())) {
      throw new IllegalArgumentException("아이디, 비밀번호를 확인해주세요");
    }

    // 2. 아이디 중복일 때 → 409
    if (userRepository.existsByUserId(request.getUserId())) {
      throw new DuplicateKeyException("이미 사용중인 아이디입니다.");
    }

    // 3. 닉네임 중복일 때 → 409
    if (userRepository.existsByNickname(request.getNickname())) {
      throw new DuplicateKeyException("이미 사용중인 닉네임입니다.");
    }

    String encodedPassword = passwordEncoder.encode(request.getPassword());

    User user =
        User.builder()
            .userId(request.getUserId())
            .password(encodedPassword)
            .nickname(request.getNickname())
            .gender(request.getGender())
            .height(request.getHeight())
            .weight(request.getWeight())
            .provider(Provider.LOCAL)
            .build();

    userRepository.save(user);
  }
}
