package sopt.thingkerton.global.code;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum GlobalErrorCode implements ErrorCode {
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "오류가 발생했습니다. 다시 시도해 주세요."),
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "유효하지 않은 값입니다."),
    INVALID_PAGE(HttpStatus.BAD_REQUEST, "page는 1 이상이어야 합니다."),
    INVALID_PAGE_SIZE(HttpStatus.BAD_REQUEST, "size는 양수여야 합니다.");

    private final HttpStatus status;
    private final String message;

    GlobalErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
