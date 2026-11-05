package sopt.thingkerton.domain.ai.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import sopt.thingkerton.domain.ai.code.AiSuccessCode;
import sopt.thingkerton.domain.ai.dto.AiTestRequest;
import sopt.thingkerton.domain.ai.dto.AiTestResponse;
import sopt.thingkerton.global.dto.CommonResponse;
import sopt.thingkerton.global.infra.ai.AiClient;
import sopt.thingkerton.global.infra.ai.AiImage;

/**
 * Gemini 연동 확인용 임시 API. 실제 기능 API가 생기면 삭제한다.
 */
@Tag(name = "AI Test", description = "Gemini 연동 확인용 (임시)")
@RestController
@RequestMapping("/api/ai/test")
@RequiredArgsConstructor
public class AiTestController {

    private final AiClient aiClient;

    @Operation(summary = "텍스트 대화 테스트")
    @PostMapping
    public CommonResponse<AiTestResponse> chat(@Valid @RequestBody AiTestRequest request) {
        String answer = aiClient.chat(request.systemPrompt(), request.message());
        return CommonResponse.success(AiSuccessCode.AI_TEST_SUCCESS, new AiTestResponse(answer));
    }

    @Operation(summary = "이미지 대화 테스트", description = "jpg, png, webp 이미지와 메시지를 함께 보냅니다.")
    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CommonResponse<AiTestResponse> chatWithImage(
            @RequestPart("file") MultipartFile file,
            @RequestParam(defaultValue = "이 사진에 무엇이 있는지 한 문장으로 설명해줘.") String message) {
        String answer = aiClient.chatWithImage(null, message, AiImage.from(file));
        return CommonResponse.success(AiSuccessCode.AI_TEST_SUCCESS, new AiTestResponse(answer));
    }
}
