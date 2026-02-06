package com.ssafy.closetory.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.closetory.dto.common.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final JwtTokenProvider jwtTokenProvider;
  private final ObjectMapper objectMapper;

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
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

        // 인가 설정
        .authorizeHttpRequests(
            auth ->
                auth

                    //  Swagger 관련 허용
                    .requestMatchers(
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/v3/api-docs/**",
                        "/api/v1/swagger-ui/**",
                        "/api/v1/v3/api-docs/**")
                    .permitAll()

                    //  인증 관련 API 허용
                    .requestMatchers("/auth/**", "/api/v1/auth/**")
                    .permitAll()

                    //  나머지는 JWT 필요
                    .anyRequest()
                    .authenticated())

        // JWT 인증 필터
        .addFilterBefore(
            new JwtAuthenticationFilter(jwtTokenProvider),
            UsernamePasswordAuthenticationFilter.class)
        .exceptionHandling(
            eh ->
                eh.authenticationEntryPoint(
                    (req, res, ex) -> {
                      res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                      res.setContentType(MediaType.APPLICATION_JSON_VALUE);
                      res.setCharacterEncoding("UTF-8");

                      String body =
                          objectMapper.writeValueAsString(
                              ApiResponse.fail(HttpServletResponse.SC_UNAUTHORIZED, "로그인이 필요합니다."));
                      res.getWriter().write(body);
                    }));

    return http.build();
  }
}
