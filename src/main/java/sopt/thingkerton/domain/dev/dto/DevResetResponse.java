package sopt.thingkerton.domain.dev.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record DevResetResponse(
        @Schema(description = "삭제한 퀘스트 수 (오늘 묶음이 없으면 0)", example = "3")
        int deletedQuestCount
) {

    public static DevResetResponse from(int deletedQuestCount) {
        return new DevResetResponse(deletedQuestCount);
    }
}
