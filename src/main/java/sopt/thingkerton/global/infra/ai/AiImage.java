package sopt.thingkerton.global.infra.ai;

import java.io.IOException;
import java.util.Set;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.util.MimeType;
import org.springframework.web.multipart.MultipartFile;
import sopt.thingkerton.global.exception.BusinessException;

/**
 * AI에 함께 보낼 이미지. 퀘스트 인증 사진처럼 업로드된 파일을 그대로 전달할 때 사용한다.
 */
public record AiImage(
        Resource resource,
        MimeType mimeType
) {

    private static final Set<String> SUPPORTED_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

    public static AiImage of(byte[] data, String contentType) {
        if (data == null || data.length == 0 || contentType == null || !SUPPORTED_TYPES.contains(contentType)) {
            throw new BusinessException(AiErrorCode.INVALID_AI_IMAGE);
        }
        return new AiImage(new ByteArrayResource(data), MimeType.valueOf(contentType));
    }

    public static AiImage from(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(AiErrorCode.INVALID_AI_IMAGE);
        }
        try {
            return of(file.getBytes(), file.getContentType());
        } catch (IOException exception) {
            throw new BusinessException(AiErrorCode.INVALID_AI_IMAGE);
        }
    }
}
