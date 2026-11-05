package sopt.thingkerton.global.infra.ai;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 이미지 생성 설정. Spring AI 이미지 모델 대신 Gemini SDK를 직접 호출하므로 별도 설정으로 둔다.
 *
 * @param model       이미지 생성 모델 (예: gemini-2.5-flash-image)
 * @param aspectRatio 생성 이미지 비율 (예: 1:1, 4:3, 16:9)
 */
@ConfigurationProperties("app.ai.image")
public record AiImageProperties(
        String model,
        String aspectRatio
) {
}
