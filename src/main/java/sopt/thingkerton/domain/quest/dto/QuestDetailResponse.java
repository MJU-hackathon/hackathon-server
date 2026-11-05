package sopt.thingkerton.domain.quest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "퀘스트 상세(WF-03)")
public record QuestDetailResponse(
        @Schema(description = "퀘스트 ID", example = "2")
        Long questId,

        @Schema(description = "오늘 퀘스트 묶음 ID", example = "1")
        Long dailySetId,

        @Schema(description = "장소 정규화 키 (화면 표시용 아님)", example = "yeongcheon-market")
        String placeKey,

        @Schema(description = "활동 정규화 키 (화면 표시용 아님)", example = "order-market-food")
        String activityKey,

        @Schema(description = "카테고리", example = "FOOD")
        Interest category,

        @Schema(description = "예상 소요 시간(분)", example = "40")
        int estimatedMinutes,

        @Schema(description = "퀘스트 제목", example = "영천시장 수제 꽈배기 맛보기")
        String title,

        @Schema(description = "추천 근거 한 줄", example = "맛있는 음식을 즐기는 당신을 위한 전통시장 간식 탐방!")
        String reason,

        @Schema(description = "퀘스트 소개 1~2문장", example = "서대문 영천시장에서 한국 시장 음식을 직접 골라 주문해 보는 퀘스트예요.")
        String description,

        @Schema(description = "오늘의 작은 도전 단계 (보통 3개, 마지막은 사진 찍기)",
                example = "[\"시장에서 먹어보고 싶은 음식 찾기\", \"한국어로 주문해 보기\", \"음식이나 시장 풍경 사진 찍기\"]")
        List<String> steps,

        @Schema(description = "장소명. AI 생성값이며 번역·실존 검증하지 않음. 좌표는 제공하지 않음", example = "영천시장")
        String placeName,

        @Schema(description = "대표 이미지 URL (16:10 영역)", example = "https://cdn.example.com/quest-images/food.jpg")
        String imageUrl,

        @Schema(description = "설명의 실제 언어. 번역 실패 시 요청 언어와 다를 수 있음", example = "ko", type = "string",
                allowableValues = {"ko", "en", "vi", "zh", "ja"})
        AppLocale locale,

        @Schema(description = "조회 시점 상태. AVAILABLE이 아니면 패스·기록 버튼 비활성화", example = "AVAILABLE")
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
