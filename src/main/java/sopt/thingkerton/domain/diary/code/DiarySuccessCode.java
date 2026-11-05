package sopt.thingkerton.domain.diary.code;

import lombok.Getter;
import sopt.thingkerton.global.code.SuccessCode;

@Getter
public enum DiarySuccessCode implements SuccessCode {
    CREATE_DIARY("일기를 저장했습니다."),
    GET_DIARIES("일기 목록 조회에 성공했습니다.");

    private final String message;

    DiarySuccessCode(String message) {
        this.message = message;
    }
}
