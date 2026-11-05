package sopt.thingkerton.domain.quest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import sopt.thingkerton.domain.quest.entity.QuestStatus;
import sopt.thingkerton.domain.quest.service.PassResult;

@Schema(description = "패스 결과")
public record QuestPassResponse(
        @Schema(description = "패스한 퀘스트 ID", example = "3")
        Long questId,

        @Schema(description = "변경된 상태 (항상 SKIPPED)", example = "SKIPPED")
        QuestStatus status,

        @Schema(description = "패스 후 오늘 남은 퀘스트 수 (0~2)", example = "2")
        long remainingCount
) {

    public static QuestPassResponse from(PassResult result) {
        return new QuestPassResponse(result.quest().getId(), result.quest().getStatus(), result.remainingCount());
    }
}
