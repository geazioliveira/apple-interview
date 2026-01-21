package org.example.appleinterview.configs;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.example.appleinterview.dtos.WeatherResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

@Configuration
public class AppConfig {

    @Bean
    WebClient webClient(@Value("${app.http.user-agent}") String userAgent) {
        return WebClient.builder()
                .defaultHeader(HttpHeaders.USER_AGENT, userAgent)
                .build();
    }

    @Bean
    com.github.benmanes.caffeine.cache.Cache<String, WeatherResponse> weatherCache (
            @Value("${app.cache.ttl-minutes}") long ttlMinutes
    ) {
        return Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(ttlMinutes))
                .maximumSize(10_000)
                .build();
    }

}
