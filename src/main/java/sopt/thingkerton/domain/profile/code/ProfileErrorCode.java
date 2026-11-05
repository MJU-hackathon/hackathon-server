package sopt.thingkerton.domain.profile.code;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import sopt.thingkerton.global.code.ErrorCode;

@Getter
public enum ProfileErrorCode implements ErrorCode {
    PROFILE_NOT_FOUND(HttpStatus.NOT_FOUND, "프로필을 먼저 설정해 주세요."),
    INVALID_NICKNAME(HttpStatus.BAD_REQUEST, "닉네임은 2~12자로 입력해 주세요."),
    INVALID_STAY_PERIOD(HttpStatus.BAD_REQUEST, "체류 종료일은 시작일 이후여야 합니다."),
    INTEREST_REQUIRED(HttpStatus.BAD_REQUEST, "관심사를 1개 이상 선택해 주세요."),
    UNSUPPORTED_LOCALE(HttpStatus.BAD_REQUEST, "지원하지 않는 언어입니다.");

    private final HttpStatus status;
    private final String message;

    ProfileErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
