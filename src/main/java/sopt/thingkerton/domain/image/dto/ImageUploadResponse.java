package sopt.thingkerton.domain.image.dto;

import sopt.thingkerton.global.infra.s3.UploadedFile;

public record ImageUploadResponse(
        String url
) {

    public static ImageUploadResponse from(UploadedFile uploadedFile) {
        return new ImageUploadResponse(uploadedFile.url());
    }
}
