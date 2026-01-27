package com.ssafy.closetory.config.webclient;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

// 용도 : fastApi 통신
@Configuration
public class WebClientConfig {

  @Bean
  public WebClient fastApiWebClient() {
    ExchangeStrategies exchangeStrategies =
        ExchangeStrategies.builder()
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(20 * 1024 * 1024))
            .build();

    return WebClient.builder()
        .baseUrl("http://localhost:8000")
        .exchangeStrategies(exchangeStrategies)
        .build();
  }
}
