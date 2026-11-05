package sopt.thingkerton.domain.image.code;

import lombok.Getter;
import sopt.thingkerton.global.code.SuccessCode;

@Getter
public enum ImageSuccessCode implements SuccessCode {
    IMAGE_UPLOAD_SUCCESS("이미지를 업로드했습니다.");

    private final String message;

    ImageSuccessCode(String message) {
        this.message = message;
    }
}
