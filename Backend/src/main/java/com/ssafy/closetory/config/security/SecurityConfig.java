package com.ssafy.closetory.config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final JwtTokenProvider jwtTokenProvider;

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

    http
      // CSRF 비활성화 (JWT 사용)
      .csrf(AbstractHttpConfigurer::disable)

      // 세션 미사용
      .sessionManagement(session ->
        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
      )

      // 인가 설정
      .authorizeHttpRequests(auth -> auth

        //  Swagger 관련 허용
        .requestMatchers(
          "/swagger-ui/**",
          "/swagger-ui.html",
          "/v3/api-docs/**",
          "/api/v1/swagger-ui/**",
          "/api/v1/v3/api-docs/**"
        ).permitAll()

        //  인증 관련 API 허용
        .requestMatchers(
          "/auth/**",
          "/api/v1/auth/**"
        ).permitAll()

        //  나머지는 JWT 필요
        .anyRequest().authenticated()
      )

      // JWT 인증 필터
      .addFilterBefore(
        new JwtAuthenticationFilter(jwtTokenProvider),
        UsernamePasswordAuthenticationFilter.class
      );

    return http.build();
  }
}
