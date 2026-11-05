package sopt.thingkerton.domain.quest.code;

import lombok.Getter;
import sopt.thingkerton.global.code.SuccessCode;

@Getter
public enum QuestSuccessCode implements SuccessCode {
    ENSURE_DAILY_QUESTS("오늘의 퀘스트 조회에 성공했습니다."),
    GET_TODAY_QUESTS("오늘 남은 퀘스트 조회에 성공했습니다."),
    GET_QUEST_DETAIL("퀘스트 상세 조회에 성공했습니다."),
    PASS_QUEST("퀘스트를 패스했습니다.");

    private final String message;

    QuestSuccessCode(String message) {
        this.message = message;
    }
}
