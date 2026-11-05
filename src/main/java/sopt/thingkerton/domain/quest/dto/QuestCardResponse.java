package sopt.thingkerton.domain.quest.dto;

import java.util.List;
import sopt.thingkerton.domain.profile.entity.Interest;
import sopt.thingkerton.domain.quest.entity.Quest;
import sopt.thingkerton.domain.quest.service.LocalizedQuest;

/**
 * 홈(WF-02) 퀘스트 카드.
 */
public record QuestCardResponse(
        Long questId,
        Interest category,
        int estimatedMinutes,
        String title,
        String placeName,
        String imageUrl,
        String reason
) {

    public static List<QuestCardResponse> listOf(List<LocalizedQuest> quests) {
        return quests.stream().map(QuestCardResponse::from).toList();
    }

    public static QuestCardResponse from(LocalizedQuest localized) {
        Quest quest = localized.quest();
        return new QuestCardResponse(
                quest.getId(),
                quest.getCategory(),
                quest.getEstimatedMinutes(),
                localized.text().getTitle(),
                quest.getPlaceName(),
                localized.imageUrl(),
                localized.text().getReason());
    }
}
