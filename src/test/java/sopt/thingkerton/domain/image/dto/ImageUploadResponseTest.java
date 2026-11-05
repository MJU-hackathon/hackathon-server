package sopt.thingkerton.domain.image.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import sopt.thingkerton.global.infra.s3.UploadedFile;

class ImageUploadResponseTest {

    @Test
    void 업로드_결과에서_URL만_응답한다() {
        UploadedFile uploaded = new UploadedFile("uploads/abc.jpg", "https://bucket/uploads/abc.jpg");

        ImageUploadResponse response = ImageUploadResponse.from(uploaded);

        assertThat(response.url()).isEqualTo("https://bucket/uploads/abc.jpg");
    }
}
