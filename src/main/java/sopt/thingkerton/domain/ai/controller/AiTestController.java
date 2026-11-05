package sopt.thingkerton.domain.ai.controller;

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
@RestController
@RequestMapping("/api/ai/test")
@RequiredArgsConstructor
public class AiTestController implements AiTestApi {

    private final AiClient aiClient;

    @PostMapping
    @Override
    public CommonResponse<AiTestResponse> chat(@Valid @RequestBody AiTestRequest request) {
        String answer = aiClient.chat(request.systemPrompt(), request.message());
        return CommonResponse.success(AiSuccessCode.AI_TEST_SUCCESS, new AiTestResponse(answer));
    }

    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Override
    public CommonResponse<AiTestResponse> chatWithImage(
            @RequestPart("file") MultipartFile file,
            @RequestParam(defaultValue = "이 사진에 무엇이 있는지 한 문장으로 설명해줘.") String message) {
        String answer = aiClient.chatWithImage(null, message, AiImage.from(file));
        return CommonResponse.success(AiSuccessCode.AI_TEST_SUCCESS, new AiTestResponse(answer));
    }
}
