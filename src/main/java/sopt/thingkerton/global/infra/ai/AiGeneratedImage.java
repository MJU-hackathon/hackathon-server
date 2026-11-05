package sopt.thingkerton.global.infra.ai;

/**
 * AI가 생성한 이미지.
 *
 * @param contentType 예: image/png, image/jpeg
 */
public record AiGeneratedImage(
        byte[] data,
        String contentType
) {
}
