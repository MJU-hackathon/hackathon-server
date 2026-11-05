package sopt.thingkerton.global.s3;

import lombok.Getter;
import sopt.thingkerton.global.code.SuccessCode;

@Getter
public enum S3SuccessCode implements SuccessCode {
    IMAGE_UPLOAD_SUCCESS("이미지를 업로드했습니다.");

    private final String message;

    S3SuccessCode(String message) {
        this.message = message;
    }
}
