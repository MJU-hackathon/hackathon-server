package sopt.thingkerton.global.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import sopt.thingkerton.global.code.ErrorCode;
import sopt.thingkerton.global.code.GlobalErrorCode;
import sopt.thingkerton.global.dto.CommonResponse;
import sopt.thingkerton.global.infra.s3.S3ErrorCode;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<CommonResponse<?>> handleBusinessException(BusinessException exception) {
        ErrorCode errorCode = exception.getErrorCode();
        return ResponseEntity.status(errorCode.getStatus()).body(CommonResponse.error(errorCode));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CommonResponse<?>> handleException(Exception exception) {
        log.error("Unhandled exception", exception);
        ErrorCode errorCode = GlobalErrorCode.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(errorCode.getStatus()).body(CommonResponse.error(errorCode));
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<Object> handleBindException(BindException exception) {
        return handleValidationException(exception);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        return handleValidationException(exception);
    }

    @Override
    protected ResponseEntity<Object> handleMaxUploadSizeExceededException(
            MaxUploadSizeExceededException exception,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        ErrorCode errorCode = S3ErrorCode.FILE_TOO_LARGE;
        return ResponseEntity.status(errorCode.getStatus()).body(CommonResponse.error(errorCode));
    }

    private ResponseEntity<Object> handleValidationException(BindException exception) {
        String message = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .findFirst()
                .orElse("올바르지 않은 입력값입니다.");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(CommonResponse.validationError(message));
    }
}
