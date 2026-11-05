package sopt.thingkerton.global.infra.s3;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import sopt.thingkerton.global.exception.BusinessException;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3Uploader {

    private static final Map<String, String> IMAGE_EXTENSIONS = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png",
            "image/webp", "webp",
            "image/heic", "heic",
            "image/heif", "heif"
    );

    /** 기본 허용 형식. HEIC는 호출 측이 명시적으로 허용할 때만 받는다. */
    private static final Set<String> DEFAULT_IMAGE_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

    private final S3Client s3Client;
    private final S3Properties properties;

    /**
     * 이미지를 {dir}/{UUID}.{확장자} 경로로 업로드한다.
     *
     * @param dir 저장 디렉터리 (예: "quests/1/3"), 앞뒤 슬래시 없이 전달
     */
    public UploadedFile uploadImage(MultipartFile file, String dir) {
        return uploadImage(file, dir, DEFAULT_IMAGE_TYPES);
    }

    /**
     * 허용 형식을 지정해 업로드한다. (예: 일기 사진은 HEIC 허용)
     *
     * @param allowedContentTypes 허용할 Content-Type. jpeg, png, webp, heic, heif 중에서 고른다
     */
    public UploadedFile uploadImage(MultipartFile file, String dir, Set<String> allowedContentTypes) {
        String extension = validateImage(file, allowedContentTypes);
        String key = dir + "/" + UUID.randomUUID() + "." + extension;

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(properties.bucket())
                .key(key)
                .contentType(file.getContentType())
                .contentLength(file.getSize())
                .build();

        try (InputStream inputStream = file.getInputStream()) {
            s3Client.putObject(request, RequestBody.fromInputStream(inputStream, file.getSize()));
        } catch (IOException | SdkException exception) {
            log.error("S3 upload failed. key={}", key, exception);
            throw new BusinessException(S3ErrorCode.UPLOAD_FAILED);
        }

        return new UploadedFile(key, getUrl(key));
    }

    public void delete(String key) {
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(properties.bucket())
                    .key(key)
                    .build());
        } catch (SdkException exception) {
            log.warn("S3 delete failed. key={}", key, exception);
        }
    }

    /**
     * 저장된 객체 키의 URL.
     */
    public String getUrl(String key) {
        return s3Client.utilities()
                .getUrl(GetUrlRequest.builder().bucket(properties.bucket()).key(key).build())
                .toString();
    }

    private String validateImage(MultipartFile file, Set<String> allowedContentTypes) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(S3ErrorCode.EMPTY_FILE);
        }
        String contentType = file.getContentType();
        String extension = contentType != null && allowedContentTypes.contains(contentType)
                ? IMAGE_EXTENSIONS.get(contentType)
                : null;
        if (extension == null) {
            throw new BusinessException(S3ErrorCode.INVALID_IMAGE_TYPE);
        }
        return extension;
    }
}
