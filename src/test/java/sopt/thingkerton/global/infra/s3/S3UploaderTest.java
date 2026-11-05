package sopt.thingkerton.global.infra.s3;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;
import sopt.thingkerton.global.exception.BusinessException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Utilities;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

class S3UploaderTest {

    private static final String BUCKET = "test-bucket";

    private S3Client s3Client;
    private S3Uploader s3Uploader;

    @BeforeEach
    void setUp() {
        s3Client = mock(S3Client.class);
        when(s3Client.utilities()).thenReturn(S3Utilities.builder().region(Region.AP_SOUTHEAST_2).build());
        s3Uploader = new S3Uploader(s3Client, new S3Properties(BUCKET, "ap-southeast-2"));
    }

    @Test
    void 이미지를_업로드하고_URL을_반환한다() {
        MockMultipartFile file = new MockMultipartFile("file", "사진.jpeg", "image/jpeg", new byte[]{1, 2, 3});

        UploadedFile result = s3Uploader.uploadImage(file, "uploads");

        ArgumentCaptor<PutObjectRequest> captor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client).putObject(captor.capture(), any(RequestBody.class));
        PutObjectRequest request = captor.getValue();

        assertThat(request.bucket()).isEqualTo(BUCKET);
        assertThat(request.contentType()).isEqualTo("image/jpeg");
        assertThat(result.key()).startsWith("uploads/").endsWith(".jpg").isEqualTo(request.key());
        assertThat(result.url())
                .isEqualTo("https://test-bucket.s3.ap-southeast-2.amazonaws.com/" + result.key());
    }

    @Test
    void 바이트_배열_이미지를_업로드하고_URL을_반환한다() {
        UploadedFile result = s3Uploader.uploadImage(new byte[]{1, 2, 3}, "image/png", "quest-images");

        ArgumentCaptor<PutObjectRequest> captor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client).putObject(captor.capture(), any(RequestBody.class));
        assertThat(captor.getValue().contentType()).isEqualTo("image/png");
        assertThat(captor.getValue().contentLength()).isEqualTo(3L);
        assertThat(result.key()).startsWith("quest-images/").endsWith(".png").isEqualTo(captor.getValue().key());
        assertThat(result.url()).isEqualTo("https://test-bucket.s3.ap-southeast-2.amazonaws.com/" + result.key());
    }

    @Test
    void 바이트_배열이_비었거나_형식이_다르면_업로드하지_않는다() {
        assertThatThrownBy(() -> s3Uploader.uploadImage(new byte[0], "image/png", "quest-images"))
                .extracting("errorCode").isEqualTo(S3ErrorCode.EMPTY_FILE);
        assertThatThrownBy(() -> s3Uploader.uploadImage(new byte[]{1}, "image/gif", "quest-images"))
                .extracting("errorCode").isEqualTo(S3ErrorCode.INVALID_IMAGE_TYPE);
        verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void 허용_형식을_지정하면_HEIC도_업로드한다() {
        MockMultipartFile file = new MockMultipartFile("file", "photo.heic", "image/heic", new byte[]{1, 2, 3});

        UploadedFile result = s3Uploader.uploadImage(file, "diaries/u1", Set.of("image/jpeg", "image/heic"));

        assertThat(result.key()).startsWith("diaries/u1/").endsWith(".heic");
        assertThatThrownBy(() -> s3Uploader.uploadImage(
                new MockMultipartFile("file", "a.webp", "image/webp", new byte[]{1}), "diaries/u1", Set.of("image/heic")))
                .extracting("errorCode").isEqualTo(S3ErrorCode.INVALID_IMAGE_TYPE);
    }

    @Test
    void 빈_파일이면_EMPTY_FILE_예외가_발생한다() {
        MockMultipartFile file = new MockMultipartFile("file", "empty.jpg", "image/jpeg", new byte[0]);

        assertThatThrownBy(() -> s3Uploader.uploadImage(file, "uploads"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(S3ErrorCode.EMPTY_FILE);
        verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @ParameterizedTest
    @ValueSource(strings = {"image/heic", "image/gif", "text/plain", "application/octet-stream"})
    void 허용하지_않는_타입이면_INVALID_IMAGE_TYPE_예외가_발생한다(String contentType) {
        MockMultipartFile file = new MockMultipartFile("file", "file", contentType, new byte[]{1});

        assertThatThrownBy(() -> s3Uploader.uploadImage(file, "uploads"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(S3ErrorCode.INVALID_IMAGE_TYPE);
    }

    @Test
    void S3_업로드에_실패하면_UPLOAD_FAILED_예외가_발생한다() {
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenThrow(SdkClientException.create("network error"));
        MockMultipartFile file = new MockMultipartFile("file", "a.png", "image/png", new byte[]{1});

        assertThatThrownBy(() -> s3Uploader.uploadImage(file, "uploads"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(S3ErrorCode.UPLOAD_FAILED);
    }
}
