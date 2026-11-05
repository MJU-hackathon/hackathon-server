package sopt.thingkerton.domain.image.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import sopt.thingkerton.domain.image.dto.ImageUploadResponse;
import sopt.thingkerton.global.infra.s3.S3Uploader;
import sopt.thingkerton.global.infra.s3.UploadedFile;

class ImageServiceTest {

    private final S3Uploader s3Uploader = mock(S3Uploader.class);
    private final ImageService imageService = new ImageService(s3Uploader);

    @Test
    void uploads_디렉터리에_업로드하고_URL만_응답한다() {
        MockMultipartFile file = new MockMultipartFile("file", "a.jpg", "image/jpeg", new byte[]{1});
        when(s3Uploader.uploadImage(file, "uploads"))
                .thenReturn(new UploadedFile("uploads/abc.jpg", "https://bucket/uploads/abc.jpg"));

        ImageUploadResponse response = imageService.upload(file);

        assertThat(response.url()).isEqualTo("https://bucket/uploads/abc.jpg");
    }
}
