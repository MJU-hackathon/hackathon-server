package sopt.thingkerton.domain.ai.code;

import lombok.Getter;
import sopt.thingkerton.global.code.SuccessCode;

@Getter
public enum AiSuccessCode implements SuccessCode {
    AI_TEST_SUCCESS("AI 응답을 받았습니다.");

    private final String message;

    AiSuccessCode(String message) {
        this.message = message;
    }
}
