package sopt.thingkerton.domain.quest.dto;

import java.math.BigDecimal;
import java.util.List;
import sopt.thingkerton.domain.profile.entity.AppLocale;
import sopt.thingkerton.domain.profile.entity.Interest;
import sopt.thingkerton.domain.quest.entity.Quest;
import sopt.thingkerton.domain.quest.entity.QuestStatus;
import sopt.thingkerton.domain.quest.entity.QuestText;
import sopt.thingkerton.domain.quest.service.LocalizedQuest;

/**
 * 퀘스트 상세(WF-03).
 *
 * @param address   장소 도로명 주소 (AI 생성값, 없으면 null)
 * @param latitude  주소를 네이버 Geocoding으로 변환한 위도 (변환 실패 시 null)
 * @param longitude 주소를 네이버 Geocoding으로 변환한 경도 (변환 실패 시 null)
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
        String address,
        Double latitude,
        Double longitude,
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
                quest.getAddress(),
                toDouble(quest.getLatitude()),
                toDouble(quest.getLongitude()),
                localized.imageUrl(),
                text.getAppLocale(),
                localized.status());
    }

    private static Double toDouble(BigDecimal value) {
        return value == null ? null : value.doubleValue();
    }
}
