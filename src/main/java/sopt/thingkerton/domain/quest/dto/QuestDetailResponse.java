package sopt.thingkerton.domain.quest.dto;

import java.util.List;
import sopt.thingkerton.domain.profile.entity.AppLocale;
import sopt.thingkerton.domain.profile.entity.Interest;
import sopt.thingkerton.domain.quest.entity.Quest;
import sopt.thingkerton.domain.quest.entity.QuestStatus;
import sopt.thingkerton.domain.quest.entity.QuestText;
import sopt.thingkerton.domain.quest.service.LocalizedQuest;

/**
 * 퀘스트 상세(WF-03). 지도 API를 쓰지 않으므로 좌표는 내려주지 않는다.
 *
 * @param locale 설명의 실제 언어 (번역 실패 시 요청 언어와 다를 수 있음)
 */
public record QuestDetailResponse(
        Long questId,
        Long dailySetId,
        String placeKey,
        String activityKey,
        Interest category,
        int estimatedMinutes,
        String title,
        String reason,
        String description,
        List<String> steps,
        String placeName,
        String imageUrl,
        AppLocale locale,
        QuestStatus status
) {

    public static QuestDetailResponse from(LocalizedQuest localized) {
        Quest quest = localized.quest();
        QuestText text = localized.text();
        return new QuestDetailResponse(
                quest.getId(),
                quest.getDailySet().getId(),
                quest.getPlaceKey(),
                quest.getActivityKey(),
                quest.getCategory(),
                quest.getEstimatedMinutes(),
                text.getTitle(),
                text.getReason(),
                text.getDescription(),
                text.getSteps(),
                quest.getPlaceName(),
                localized.imageUrl(),
                text.getAppLocale(),
                localized.status());
    }
}
