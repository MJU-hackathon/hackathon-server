package sopt.thingkerton.domain.quest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import sopt.thingkerton.domain.profile.entity.Interest;
import sopt.thingkerton.domain.quest.entity.Quest;
import sopt.thingkerton.domain.quest.service.LocalizedQuest;

/**
 * 홈(WF-02) 퀘스트 카드.
 */
@Schema(description = "홈(WF-02) 퀘스트 카드")
public record QuestCardResponse(
        @Schema(description = "퀘스트 ID. 카드 탭 시 상세 조회에 사용", example = "2")
        Long questId,

        @Schema(description = "카테고리 (1행 태그)", example = "FOOD")
        Interest category,

        @Schema(description = "예상 소요 시간(분), 10~240 (1행 시계)", example = "40")
        int estimatedMinutes,

        @Schema(description = "퀘스트 제목 (요청 언어)", example = "영천시장 수제 꽈배기 맛보기")
        String title,

        @Schema(description = "장소명. AI 생성값이며 실존 검증·번역하지 않음", example = "영천시장")
        String placeName,

        @Schema(description = "대표 이미지 URL (팀 준비 이미지, 카테고리별)", example = "https://cdn.example.com/quest-images/food.jpg")
        String imageUrl,

        @Schema(description = "추천 근거 한 줄 (요청 언어)", example = "맛있는 음식을 즐기는 당신을 위한 전통시장 간식 탐방!")
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
