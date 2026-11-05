package sopt.thingkerton.domain.quest.code;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import sopt.thingkerton.global.code.ErrorCode;

@Getter
public enum QuestErrorCode implements ErrorCode {
    DAILY_SET_NOT_FOUND(HttpStatus.NOT_FOUND, "오늘의 퀘스트가 아직 없습니다."),
    QUEST_NOT_FOUND(HttpStatus.NOT_FOUND, "퀘스트를 찾을 수 없습니다."),
    QUEST_NOT_AVAILABLE(HttpStatus.CONFLICT, "이미 패스했거나 기록했거나 종료된 퀘스트입니다.");

    private final HttpStatus status;
    private final String message;

    QuestErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
