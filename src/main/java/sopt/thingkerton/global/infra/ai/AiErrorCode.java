package sopt.thingkerton.global.infra.ai;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import sopt.thingkerton.global.code.ErrorCode;

@Getter
public enum AiErrorCode implements ErrorCode {
    AI_RATE_LIMITED(HttpStatus.TOO_MANY_REQUESTS, "AI 요청이 많아 잠시 후 다시 시도해 주세요."),
    AI_CALL_FAILED(HttpStatus.SERVICE_UNAVAILABLE, "AI 응답을 받지 못했습니다. 다시 시도해 주세요."),
    AI_TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, "AI 응답이 늦어지고 있습니다. 잠시 후 다시 시도해 주세요."),
    INVALID_AI_IMAGE(HttpStatus.BAD_REQUEST, "AI에 전달할 이미지는 jpg, png, webp만 가능합니다.");

    private final HttpStatus status;
    private final String message;

    AiErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
