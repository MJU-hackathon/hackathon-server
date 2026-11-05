package sopt.thingkerton.global.infra.ai;

import com.google.genai.errors.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import sopt.thingkerton.global.exception.BusinessException;

/**
 * Gemini 호출 예외를 {@link AiErrorCode} 기반 {@link BusinessException}으로 변환한다.
 */
@Slf4j
final class AiExceptions {

    private AiExceptions() {
    }

    static BusinessException translate(RuntimeException exception) {
        if (exception instanceof BusinessException businessException) {
            return businessException;
        }
        ApiException apiException = findApiException(exception);
        if (apiException != null && apiException.code() == HttpStatus.TOO_MANY_REQUESTS.value()) {
            log.warn("AI rate limited: {}", apiException.message());
            return new BusinessException(AiErrorCode.AI_RATE_LIMITED);
        }
        log.error("AI call failed", exception);
        return new BusinessException(AiErrorCode.AI_CALL_FAILED);
    }

    private static ApiException findApiException(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof ApiException apiException) {
                return apiException;
            }
            current = current.getCause();
        }
        return null;
    }
}
