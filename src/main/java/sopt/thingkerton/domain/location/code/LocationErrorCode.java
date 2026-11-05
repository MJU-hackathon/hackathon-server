package sopt.thingkerton.domain.location.code;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import sopt.thingkerton.global.code.ErrorCode;

@Getter
public enum LocationErrorCode implements ErrorCode {
    INVALID_ADDRESS(HttpStatus.BAD_REQUEST, "주소를 입력해 주세요."),
    ADDRESS_NOT_FOUND(HttpStatus.NOT_FOUND, "주소를 찾을 수 없습니다."),
    GEOCODING_FAILED(HttpStatus.BAD_GATEWAY, "주소 좌표 변환에 실패했습니다."),
    INVALID_COORDINATE(HttpStatus.BAD_REQUEST, "올바른 위도와 경도를 입력해 주세요.");

    private final HttpStatus status;
    private final String message;

    LocationErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
