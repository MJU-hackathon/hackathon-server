package sopt.thingkerton.domain.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "AI 답변")
public record AiTestResponse(
        @Schema(description = "AI 답변 텍스트", example = "비 오는 날 한국에서는 파전에 막걸리를 즐겨.")
        String answer
) {
}
