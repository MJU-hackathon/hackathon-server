package sopt.thingkerton.domain.image.dto;

public record ImageUploadResponse(
        String url
) {

    public static ImageUploadResponse from(String url) {
        return new ImageUploadResponse(url);
    }
}
