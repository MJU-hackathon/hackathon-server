package sopt.thingkerton.domain.image.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.multipart.MultipartFile;
import sopt.thingkerton.domain.image.dto.ImageUploadResponse;
import sopt.thingkerton.global.dto.CommonResponse;

@Tag(name = "Image", description = "이미지 업로드")
public interface ImageApi {

    @Operation(
            summary = "이미지 업로드",
            description = "인증 헤더 없이 multipart/form-data의 file 파트로 jpg/png/webp 이미지 1장을 업로드하고 S3 URL을 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "400", description = "EMPTY_FILE: file 파트가 비었음 | INVALID_IMAGE_TYPE: jpg/png/webp 이외 형식"),
            @ApiResponse(responseCode = "413", description = "FILE_TOO_LARGE: 서버 multipart 제한(10MB) 초과"),
            @ApiResponse(responseCode = "500", description = "UPLOAD_FAILED: S3 업로드 실패 | INTERNAL_SERVER_ERROR: 처리되지 않은 서버 오류")
    })
    CommonResponse<ImageUploadResponse> upload(MultipartFile file);
}
