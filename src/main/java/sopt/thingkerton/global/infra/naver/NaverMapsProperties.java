package sopt.thingkerton.global.infra.naver;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.naver-maps")
public record NaverMapsProperties(
        String apiKeyId,
        String apiKey
) {
}