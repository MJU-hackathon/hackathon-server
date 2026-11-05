package sopt.thingkerton.global.infra.ai;

import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import sopt.thingkerton.global.exception.BusinessException;

/**
 * LLM 호출 공통 진입점. 도메인 코드는 Spring AI나 Gemini SDK에 직접 의존하지 않고 이 클래스만 사용한다.
 * <p>
 * 실패하면 {@link AiErrorCode}로 변환한 {@link BusinessException}을 던진다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AiClient {

    private final ChatClient chatClient;

    /**
     * 텍스트 대화.
     *
     * @param systemPrompt AI 역할·말투 지시 (없으면 null)
     */
    public String chat(String systemPrompt, String userMessage) {
        return execute(() -> callText(systemPrompt, userMessage, null));
    }

    /**
     * 이미지를 함께 보내는 대화. 퀘스트 인증 사진 확인 등에 사용한다.
     */
    public String chatWithImage(String systemPrompt, String userMessage, AiImage image) {
        return execute(() -> callText(systemPrompt, userMessage, image));
    }

    /**
     * 응답을 지정한 타입(record 등)으로 받는다. JSON 형식 지시는 자동으로 사용자 메시지 뒤에 추가된다.
     */
    public <T> T chat(String systemPrompt, String userMessage, Class<T> responseType) {
        return execute(() -> callEntity(systemPrompt, userMessage, null, responseType));
    }

    /**
     * 이미지를 함께 보내고 응답을 지정한 타입으로 받는다. 인증 성공 여부 판정 등에 사용한다.
     */
    public <T> T chatWithImage(String systemPrompt, String userMessage, AiImage image, Class<T> responseType) {
        return execute(() -> callEntity(systemPrompt, userMessage, image, responseType));
    }

    private <T> T callEntity(String systemPrompt, String userMessage, AiImage image, Class<T> responseType) {
        BeanOutputConverter<T> converter = new BeanOutputConverter<>(responseType);
        String text = callText(systemPrompt, userMessage + "\n\n" + converter.getFormat(), image);
        return text == null ? null : converter.convert(text);
    }

    /**
     * Gemini는 답변 하나를 여러 part로 나눠 보내고, Spring AI는 part마다 Generation을 만든다.
     * {@code call().content()}는 첫 Generation만 반환해 답변이 잘리므로 모든 Generation의 텍스트를 이어 붙인다.
     */
    private String callText(String systemPrompt, String userMessage, AiImage image) {
        ChatResponse response = request(systemPrompt, userMessage, image).call().chatResponse();
        if (response == null) {
            return null;
        }
        String text = response.getResults().stream()
                .map(generation -> generation.getOutput().getText())
                .filter(Objects::nonNull)
                .collect(Collectors.joining());
        return StringUtils.hasText(text) ? text : null;
    }

    private ChatClient.ChatClientRequestSpec request(String systemPrompt, String userMessage, AiImage image) {
        ChatClient.ChatClientRequestSpec spec = chatClient.prompt();
        if (StringUtils.hasText(systemPrompt)) {
            spec = spec.system(systemPrompt);
        }
        if (image == null) {
            return spec.user(userMessage);
        }
        return spec.user(user -> user.text(userMessage).media(image.mimeType(), image.resource()));
    }

    private <T> T execute(Supplier<T> call) {
        T result;
        try {
            result = call.get();
        } catch (BusinessException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw AiExceptions.translate(exception);
        }
        if (result == null) {
            log.warn("AI returned empty response");
            throw new BusinessException(AiErrorCode.AI_CALL_FAILED);
        }
        return result;
    }
}
