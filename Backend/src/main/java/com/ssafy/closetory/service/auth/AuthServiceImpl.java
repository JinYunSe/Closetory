package com.ssafy.closetory.service.auth;

import com.ssafy.closetory.config.security.JwtTokenProvider;
import com.ssafy.closetory.dto.auth.LoginRequest;
import com.ssafy.closetory.dto.auth.LoginResponse;
import com.ssafy.closetory.dto.auth.SignupRequest;
import com.ssafy.closetory.entity.user.User;
import com.ssafy.closetory.enums.Provider;
import com.ssafy.closetory.exception.common.BadRequestException;
import com.ssafy.closetory.exception.common.ConflictException;
import com.ssafy.closetory.exception.common.NotFoundException;
import com.ssafy.closetory.exception.common.UnauthorizedException;
import com.ssafy.closetory.repository.UserRepository;
import com.ssafy.closetory.service.token.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtTokenProvider jwtProvider;
  private final RefreshTokenService refreshTokenService;

  // 회원가입
  @Override
  public void signup(SignupRequest request) {

    // 1. 비밀번호 불일치
    if (!request.password().equals(request.passwordConfirm())) {
      throw new BadRequestException("비밀번호가 일치하지 않습니다.");
    }

    // 2. 아이디 중복
    if (userRepository.existsByUsername(request.username())) {
      throw new ConflictException("이미 사용중인 아이디입니다.");
    }

    // 3. 닉네임 중복
    if (userRepository.existsByNickname(request.nickname())) {
      throw new ConflictException("이미 사용중인 닉네임입니다.");
    }

    // 4. 비밀번호 암호화
    String encodedPassword = passwordEncoder.encode(request.password());

    // 5. 사용자 엔티티 생성
    User user =
        User.builder()
            .username(request.username())
            .password(encodedPassword)
            .nickname(request.nickname())
            .gender(request.gender())
            .height(request.height())
            .weight(request.weight())
            .provider(Provider.LOCAL)
            .build();

    // 6. 저장
    userRepository.save(user);
  }

  // 로그인
  @Override
  public LoginResponse login(LoginRequest request) {

    // 1. 사용자 조회
    User user =
        userRepository
            .findByUsername(request.username())
            .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));

    // 2. 비밀번호 검증
    if (!passwordEncoder.matches(request.password(), user.getPassword())) {
      throw new BadRequestException("아이디 또는 비밀번호가 올바르지 않습니다.");
    }

    // 3. 토큰 생성
    String accessToken = jwtProvider.createAccessToken(user.getId());
    String refreshToken = refreshTokenService.createRefreshToken();

    // 4. Refresh Token 저장 (Redis)
    refreshTokenService.save(user.getId(), refreshToken);

    // 5. 응답
    return new LoginResponse(accessToken, refreshToken);
  }

  // 로그아웃
  @Override
  public void logout(Integer userId) {
    if (userId == null) {
      throw new UnauthorizedException("인증되지 않은 사용자입니다.");
    }

    // Redis에 저장된 refresh token 삭제
    refreshTokenService.delete(userId);
  }

  // 토큰 재발급
  @Override
  public LoginResponse token(Integer userId, String refreshToken) {

    // 1. userId 검증
    if (userId == null) {
      throw new UnauthorizedException("인증되지 않은 사용자입니다.");
    }

    // 2. Refresh Token 존재 여부
    if (refreshToken == null || refreshToken.isBlank()) {
      throw new UnauthorizedException("리프레시 토큰이 없습니다.");
    }

    // 3. Redis에 저장된 Refresh Token 조회
    String savedRefreshToken = refreshTokenService.get(userId);

    if (savedRefreshToken == null) {
      throw new UnauthorizedException("로그아웃된 사용자이거나 토큰이 만료되었습니다.");
    }

    // 4. Refresh Token 일치 여부
    if (!savedRefreshToken.equals(refreshToken)) {
      throw new UnauthorizedException("유효하지 않은 리프레시 토큰입니다.");
    }

    // 5. 새 토큰 발급
    String newAccessToken = jwtProvider.createAccessToken(userId);
    String newRefreshToken = refreshTokenService.createRefreshToken();

    // 6. Refresh Token
    refreshTokenService.save(userId, newRefreshToken);

    // 7. 응답
    return new LoginResponse(newAccessToken, newRefreshToken);
  }
}
