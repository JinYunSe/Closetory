package com.ssafy.closetory.config.webclient;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

// 용도 : fastApi 통신
@Configuration
public class WebClientConfig {

  @Bean
  public WebClient fastApiWebClient() {
    return WebClient.builder().baseUrl("http://localhost:8000").build();
  }
}
