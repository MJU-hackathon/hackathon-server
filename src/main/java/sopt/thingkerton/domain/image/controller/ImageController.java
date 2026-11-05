package sopt.thingkerton.domain.image.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import sopt.thingkerton.domain.image.code.ImageSuccessCode;
import sopt.thingkerton.domain.image.dto.ImageUploadResponse;
import sopt.thingkerton.domain.image.service.ImageService;
import sopt.thingkerton.global.dto.CommonResponse;
import sopt.thingkerton.global.infra.s3.UploadedFile;

@Tag(name = "Image", description = "이미지 업로드")
@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;

    @Operation(summary = "이미지 업로드", description = "jpg, png, webp 이미지(최대 10MB)를 S3에 업로드하고 URL을 반환합니다.")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CommonResponse<ImageUploadResponse> upload(@RequestPart("file") MultipartFile file) {
        UploadedFile uploadedFile = imageService.upload(file);
        return CommonResponse.success(ImageSuccessCode.IMAGE_UPLOAD_SUCCESS, ImageUploadResponse.from(uploadedFile));
    }
}
