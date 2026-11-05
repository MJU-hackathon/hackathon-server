package sopt.thingkerton.domain.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record AiTestRequest(
        @Schema(description = "AI 역할·말투 지시 (선택)", example = "너는 서대문구에 사는 유학생의 친근한 동네 친구야. 반말로 짧게 대답해.")
        String systemPrompt,

        @Schema(description = "사용자 메시지", example = "베트남 유학생에게 비 오는 날 파전 문화를 반쎄오와 비교해서 설명해줘.")
        @NotBlank(message = "message는 비어 있을 수 없습니다.")
        String message
) {
}
