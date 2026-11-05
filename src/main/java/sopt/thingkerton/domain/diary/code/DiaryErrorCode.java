package sopt.thingkerton.domain.diary.code;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import sopt.thingkerton.global.code.ErrorCode;

@Getter
public enum DiaryErrorCode implements ErrorCode {
    PHOTO_REQUIRED(HttpStatus.BAD_REQUEST, "사진 1장을 첨부해 주세요."),
    OUTCOME_REQUIRED(HttpStatus.BAD_REQUEST, "경험 결과를 선택해 주세요."),
    MEMO_TOO_LONG(HttpStatus.BAD_REQUEST, "메모는 50자까지 쓸 수 있어요."),
    INVALID_PHOTO(HttpStatus.BAD_REQUEST, "사진은 jpeg, png, heic 형식, 10MB 이하만 올릴 수 있어요."),
    ALREADY_RECORDED(HttpStatus.CONFLICT, "이미 기록한 퀘스트입니다.");

    private final HttpStatus status;
    private final String message;

    DiaryErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
