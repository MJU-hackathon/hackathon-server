package sopt.thingkerton.domain.image.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.multipart.MultipartFile;
import sopt.thingkerton.domain.image.dto.ImageUploadResponse;
import sopt.thingkerton.global.dto.CommonResponse;

@Tag(name = "Image", description = "범용 이미지 업로드. 일기 사진은 이 API가 아니라 `POST /api/diaries`에 함께 보냅니다.")
public interface ImageApi {

    @Operation(
            summary = "이미지 업로드",
            description = """
                    이미지 1장을 S3 `uploads/{UUID}.{확장자}` 경로에 올리고 공개 URL을 반환합니다. `X-User-Key`는 필요 없습니다.

                    | 항목 | 규칙 | 위반 시 |
                    | --- | --- | --- |
                    | file | 비어 있지 않음 | `400 EMPTY_FILE` |
                    | 형식 | jpg · png · webp (Content-Type 기준) | `400 INVALID_IMAGE_TYPE` |
                    | 크기 | 10MB 이하 | `413 FILE_TOO_LARGE` |
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "업로드 성공", content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(name = "업로드됨", value = """
                            {"status":200,"code":"IMAGE_UPLOAD_SUCCESS","message":"이미지를 업로드했습니다.","data":{"url":"https://mju-hackathon-photos-2026.s3.ap-southeast-2.amazonaws.com/uploads/0f8e2c1a-3b4d-4e5f-9a6b-7c8d9e0f1a2b.jpg"}}
                            """))),
            @ApiResponse(responseCode = "400", description = "빈 파일 또는 허용하지 않는 형식", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "EMPTY_FILE", value = """
                            {"status":400,"code":"EMPTY_FILE","message":"업로드할 파일이 비어 있습니다.","data":null}
                            """),
                    @ExampleObject(name = "INVALID_IMAGE_TYPE", value = """
                            {"status":400,"code":"INVALID_IMAGE_TYPE","message":"jpg, png, webp 이미지만 업로드할 수 있습니다.","data":null}
                            """)
            })),
            @ApiResponse(responseCode = "413", description = "10MB 초과", content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(name = "FILE_TOO_LARGE", value = """
                            {"status":413,"code":"FILE_TOO_LARGE","message":"파일 크기는 10MB 이하여야 합니다.","data":null}
                            """))),
            @ApiResponse(responseCode = "500", description = "S3 업로드 실패", content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(name = "UPLOAD_FAILED", value = """
                            {"status":500,"code":"UPLOAD_FAILED","message":"파일 업로드에 실패했습니다. 다시 시도해 주세요.","data":null}
                            """)))
    })
    CommonResponse<ImageUploadResponse> upload(
            @Parameter(description = "업로드할 이미지 (jpg · png · webp, 최대 10MB)", required = true) MultipartFile file);
}
