package sopt.thingkerton.domain.quest.dto;

import sopt.thingkerton.domain.quest.entity.QuestStatus;
import sopt.thingkerton.domain.quest.service.PassResult;

public record QuestPassResponse(
        Long questId,
        QuestStatus status,
        long remainingCount
) {

    public static QuestPassResponse from(PassResult result) {
        return new QuestPassResponse(result.quest().getId(), result.quest().getStatus(), result.remainingCount());
    }
}
