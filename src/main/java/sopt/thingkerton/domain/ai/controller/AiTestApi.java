package sopt.thingkerton.domain.ai.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.multipart.MultipartFile;
import sopt.thingkerton.domain.ai.dto.AiTestRequest;
import sopt.thingkerton.domain.ai.dto.AiTestResponse;
import sopt.thingkerton.global.dto.CommonResponse;

@Tag(name = "AI Test", description = "Gemini 연동 확인용 임시 API")
public interface AiTestApi {

    @Operation(summary = "텍스트 대화 테스트", description = "인증 헤더 없이 Gemini에 텍스트 메시지를 보냅니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "400", description = "INVALID_INPUT: message가 없거나 공백이거나 요청 JSON 형식이 잘못됨"),
            @ApiResponse(responseCode = "429", description = "AI_RATE_LIMITED: Gemini 요청 한도 초과"),
            @ApiResponse(responseCode = "503", description = "AI_CALL_FAILED: Gemini 호출 실패 또는 빈 응답"),
            @ApiResponse(responseCode = "500", description = "INTERNAL_SERVER_ERROR: 처리되지 않은 서버 오류")
    })
    CommonResponse<AiTestResponse> chat(AiTestRequest request);

    @Operation(summary = "이미지 대화 테스트", description = "인증 헤더 없이 jpg/png/webp 이미지와 메시지를 Gemini에 보냅니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "400", description = "INVALID_AI_IMAGE: file 파트 누락·빈 파일·지원하지 않는 이미지 형식"),
            @ApiResponse(responseCode = "413", description = "FILE_TOO_LARGE: 서버 multipart 제한(10MB) 초과"),
            @ApiResponse(responseCode = "429", description = "AI_RATE_LIMITED: Gemini 요청 한도 초과"),
            @ApiResponse(responseCode = "503", description = "AI_CALL_FAILED: Gemini 호출 실패 또는 빈 응답"),
            @ApiResponse(responseCode = "500", description = "INTERNAL_SERVER_ERROR: 처리되지 않은 서버 오류")
    })
    CommonResponse<AiTestResponse> chatWithImage(
            MultipartFile file,
            @Parameter(description = "이미지와 함께 보낼 메시지", example = "이 사진에 무엇이 있는지 한 문장으로 설명해줘.") String message
    );
}
