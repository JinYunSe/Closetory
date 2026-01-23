package com.ssafy.closetory.config.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

  private final SecretKey secretKey;
  private final long accessTokenValidityTime;

  public JwtTokenProvider(
      @Value("${jwt.secret}") String secret,
      @Value("${jwt.access-token-expire-time}") long accessTokenValidityTime) {
    this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
    this.accessTokenValidityTime = accessTokenValidityTime;
  }

  //  Access Token 생성
  public String createAccessToken(Integer userId) {
    Date now = new Date();
    Date expiry = new Date(now.getTime() + accessTokenValidityTime);

    return Jwts.builder()
        .setSubject(String.valueOf(userId)) // 토큰 주인
        .setIssuedAt(now)
        .setExpiration(expiry)
        .signWith(secretKey, SignatureAlgorithm.HS256)
        .compact();
  }

  //  토큰 유효성 검증
  public boolean validateToken(String token) {
    try {
      Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  //  토큰에서 userId 추출
  public Integer getUserId(String token) {
    Claims claims =
        Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody();

    return Integer.parseInt(claims.getSubject());
  }
}
