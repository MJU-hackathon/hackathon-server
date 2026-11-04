package sopt.thingkerton.global.dto;

import sopt.thingkerton.global.code.ErrorCode;
import sopt.thingkerton.global.code.GlobalErrorCode;
import sopt.thingkerton.global.code.SuccessCode;

public record CommonResponse<T>(
        int status,
        String code,
        String message,
        T data
) {

    public static <T> CommonResponse<T> success(SuccessCode successCode, T data) {
        return new CommonResponse<>(200, successCode.name(), successCode.getMessage(), data);
    }

    public static CommonResponse<Void> success(SuccessCode successCode) {
        return success(successCode, null);
    }

    public static CommonResponse<Void> error(ErrorCode errorCode) {
        return new CommonResponse<>(errorCode.getStatus().value(), errorCode.name(), errorCode.getMessage(), null);
    }

    public static CommonResponse<Void> validationError(String message) {
        GlobalErrorCode errorCode = GlobalErrorCode.INVALID_INPUT;
        return new CommonResponse<>(errorCode.getStatus().value(), errorCode.name(), message, null);
    }
}
