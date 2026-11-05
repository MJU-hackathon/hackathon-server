package sopt.thingkerton.domain.ai.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.multipart.MultipartFile;
import sopt.thingkerton.domain.ai.dto.AiTestRequest;
import sopt.thingkerton.domain.ai.dto.AiTestResponse;
import sopt.thingkerton.global.dto.CommonResponse;

@Tag(name = "AI Test", description = "Gemini 연동 확인용 임시 API. 앱에서는 사용하지 않으며, 실제 기능 API가 안정되면 삭제합니다.")
public interface AiTestApi {

    String AI_ERRORS = """

            **실패 응답**: 요청이 많으면 `429 AI_RATE_LIMITED`, 응답을 받지 못하면 `503 AI_CALL_FAILED`.
            """;

    @Operation(
            summary = "텍스트 대화 테스트",
            description = """
                    Gemini에 메시지를 보내고 답변 텍스트를 받습니다. `systemPrompt`로 역할·말투를 지정할 수 있습니다.
                    모델과 temperature는 서버 설정(`GEMINI_CHAT_MODEL`)을 따릅니다. `X-User-Key`는 필요 없습니다.
                    """ + AI_ERRORS
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "AI 답변", content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(name = "답변", value = """
                            {"status":200,"code":"AI_TEST_SUCCESS","message":"AI 응답을 받았습니다.","data":{"answer":"비 오는 날 한국에서는 파전에 막걸리를 즐겨. 바삭하게 부친 반쎄오랑 비슷한 느낌이야!"}}
                            """))),
            @ApiResponse(responseCode = "400", description = "message 비어 있음", content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(name = "INVALID_INPUT", value = """
                            {"status":400,"code":"INVALID_INPUT","message":"message는 비어 있을 수 없습니다.","data":null}
                            """))),
            @ApiResponse(responseCode = "429", description = "AI 요청 한도 초과", content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(name = "AI_RATE_LIMITED", value = """
                            {"status":429,"code":"AI_RATE_LIMITED","message":"AI 요청이 많아 잠시 후 다시 시도해 주세요.","data":null}
                            """))),
            @ApiResponse(responseCode = "503", description = "AI 응답 실패", content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(name = "AI_CALL_FAILED", value = """
                            {"status":503,"code":"AI_CALL_FAILED","message":"AI 응답을 받지 못했습니다. 다시 시도해 주세요.","data":null}
                            """)))
    })
    CommonResponse<AiTestResponse> chat(AiTestRequest request);

    @Operation(
            summary = "이미지 대화 테스트",
            description = """
                    이미지 1장과 메시지를 함께 보내 이미지에 대한 답변을 받습니다. jpg · png · webp만 가능합니다. `X-User-Key`는 필요 없습니다.
                    """ + AI_ERRORS
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "AI 답변", content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(name = "답변", value = """
                            {"status":200,"code":"AI_TEST_SUCCESS","message":"AI 응답을 받았습니다.","data":{"answer":"전통시장 골목에서 꽈배기를 파는 가게 사진입니다."}}
                            """))),
            @ApiResponse(responseCode = "400", description = "지원하지 않는 이미지 또는 빈 파일", content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(name = "INVALID_AI_IMAGE", value = """
                            {"status":400,"code":"INVALID_AI_IMAGE","message":"AI에 전달할 이미지는 jpg, png, webp만 가능합니다.","data":null}
                            """))),
            @ApiResponse(responseCode = "503", description = "AI 응답 실패", content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(name = "AI_CALL_FAILED", value = """
                            {"status":503,"code":"AI_CALL_FAILED","message":"AI 응답을 받지 못했습니다. 다시 시도해 주세요.","data":null}
                            """)))
    })
    CommonResponse<AiTestResponse> chatWithImage(
            @Parameter(description = "이미지 (jpg · png · webp)", required = true) MultipartFile file,
            @Parameter(description = "이미지와 함께 보낼 메시지. 생략하면 기본 질문", example = "이 사진에 무엇이 있는지 한 문장으로 설명해줘.")
            String message);
}
