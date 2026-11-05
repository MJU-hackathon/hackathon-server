package sopt.thingkerton.domain.image.controller;

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

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageController implements ImageApi {

    private final ImageService imageService;

    @Override
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CommonResponse<ImageUploadResponse> upload(@RequestPart("file") MultipartFile file) {
        UploadedFile uploadedFile = imageService.upload(file);
        return CommonResponse.success(ImageSuccessCode.IMAGE_UPLOAD_SUCCESS, ImageUploadResponse.from(uploadedFile));
    }
}
