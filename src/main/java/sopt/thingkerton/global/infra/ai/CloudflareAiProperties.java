package sopt.thingkerton.global.infra.ai;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Cloudflare Workers AI 설정. 키는 환경변수로만 받는다.
 *
 * @param accountId  Cloudflare 계정 ID
 * @param apiToken   Workers AI 권한 API 토큰
 * @param imageModel 이미지 생성 모델 (예: @cf/black-forest-labs/flux-1-schnell)
 * @param steps      diffusion step 수. 높을수록 품질이 좋아지지만 느리고 비싸다 (flux-1-schnell 최대 8)
 */
@ConfigurationProperties("app.cloudflare.ai")
public record CloudflareAiProperties(
        String accountId,
        String apiToken,
        String imageModel,
        Integer steps
) {
}
