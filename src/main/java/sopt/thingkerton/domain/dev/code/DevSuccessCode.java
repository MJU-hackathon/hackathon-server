package sopt.thingkerton.domain.dev.code;

import lombok.Getter;
import sopt.thingkerton.global.code.SuccessCode;

@Getter
public enum DevSuccessCode implements SuccessCode {
    RESET_TODAY_QUESTS("오늘 퀘스트 묶음을 초기화했습니다.");

    private final String message;

    DevSuccessCode(String message) {
        this.message = message;
    }
}
