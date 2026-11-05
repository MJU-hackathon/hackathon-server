package sopt.thingkerton.domain.location.code;

import lombok.Getter;
import sopt.thingkerton.global.code.SuccessCode;

@Getter
public enum LocationSuccessCode implements SuccessCode {
    LOCATION_SEARCH_SUCCESS("주소를 좌표로 변환했습니다.");

    private final String message;

    LocationSuccessCode(String message) {
        this.message = message;
    }
}
