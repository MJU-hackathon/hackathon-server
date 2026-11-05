package sopt.thingkerton.global.s3;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import sopt.thingkerton.global.dto.CommonResponse;

@Tag(name = "Image", description = "이미지 업로드")
@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageUploadController {

    private static final String UPLOAD_DIR = "uploads";

    private final S3Uploader s3Uploader;

    @Operation(summary = "이미지 업로드", description = "jpg, png, webp 이미지(최대 10MB)를 S3에 업로드하고 URL을 반환합니다.")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CommonResponse<UploadedFile> upload(@RequestPart("file") MultipartFile file) {
        return CommonResponse.success(S3SuccessCode.IMAGE_UPLOAD_SUCCESS, s3Uploader.uploadImage(file, UPLOAD_DIR));
    }
}
