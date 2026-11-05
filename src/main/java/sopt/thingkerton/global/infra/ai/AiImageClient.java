package sopt.thingkerton.global.infra.ai;

import com.google.genai.Client;
import com.google.genai.types.Blob;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.ImageConfig;
import com.google.genai.types.Part;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import sopt.thingkerton.global.exception.BusinessException;

/**
 * Gemini 이미지 생성. 도메인 코드는 Gemini SDK에 직접 의존하지 않고 이 클래스만 사용한다.
 * <p>
 * Spring AI가 만든 Gemini {@link Client}를 그대로 쓰므로 API 키 설정은 채팅과 같다.
 * 모델·비율은 {@link AiImageProperties}를 따른다. 실패하면 {@link AiErrorCode}로 변환한 {@link BusinessException}을 던진다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AiImageClient {

    private static final List<String> IMAGE_ONLY = List.of("IMAGE");

    private final Client genAiClient;
    private final AiImageProperties properties;

    public AiGeneratedImage generate(String prompt) {
        GenerateContentResponse response;
        try {
            response = genAiClient.models.generateContent(properties.model(), prompt, config());
        } catch (RuntimeException exception) {
            throw AiExceptions.translate(exception);
        }
        return extractImage(response);
    }

    private GenerateContentConfig config() {
        GenerateContentConfig.Builder builder = GenerateContentConfig.builder().responseModalities(IMAGE_ONLY);
        if (StringUtils.hasText(properties.aspectRatio())) {
            builder.imageConfig(ImageConfig.builder().aspectRatio(properties.aspectRatio()).build());
        }
        return builder.build();
    }

    /**
     * 응답 part 중 첫 번째 이미지를 꺼낸다. 안전 필터 등으로 이미지가 없으면 실패로 본다.
     */
    private AiGeneratedImage extractImage(GenerateContentResponse response) {
        List<Part> parts = response == null ? null : response.parts();
        if (parts != null) {
            for (Part part : parts) {
                Blob blob = part.inlineData().orElse(null);
                if (blob != null && blob.data().isPresent() && blob.data().get().length > 0) {
                    return new AiGeneratedImage(blob.data().get(), blob.mimeType().orElse("image/png"));
                }
            }
        }
        log.warn("AI image response has no image. finishReason={}",
                response == null ? null : response.finishReason());
        throw new BusinessException(AiErrorCode.AI_CALL_FAILED);
    }
}
