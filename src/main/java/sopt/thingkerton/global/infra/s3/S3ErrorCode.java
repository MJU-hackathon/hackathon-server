package sopt.thingkerton.global.infra.s3;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import sopt.thingkerton.global.code.ErrorCode;

@Getter
public enum S3ErrorCode implements ErrorCode {
    EMPTY_FILE(HttpStatus.BAD_REQUEST, "업로드할 파일이 비어 있습니다."),
    INVALID_IMAGE_TYPE(HttpStatus.BAD_REQUEST, "jpg, png, webp 이미지만 업로드할 수 있습니다."),
    FILE_TOO_LARGE(HttpStatus.PAYLOAD_TOO_LARGE, "파일 크기는 10MB 이하여야 합니다."),
    UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드에 실패했습니다. 다시 시도해 주세요.");

    private final HttpStatus status;
    private final String message;

    S3ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
