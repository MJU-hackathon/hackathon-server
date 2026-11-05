package sopt.thingkerton.domain.image.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import sopt.thingkerton.global.infra.s3.UploadedFile;

@Schema(description = "업로드 결과")
public record ImageUploadResponse(
        @Schema(description = "업로드된 이미지의 공개 URL",
                example = "https://mju-hackathon-photos-2026.s3.ap-southeast-2.amazonaws.com/uploads/0f8e2c1a-3b4d-4e5f-9a6b-7c8d9e0f1a2b.jpg")
        String url
) {

    public static ImageUploadResponse from(UploadedFile uploadedFile) {
        return new ImageUploadResponse(uploadedFile.url());
    }
}
