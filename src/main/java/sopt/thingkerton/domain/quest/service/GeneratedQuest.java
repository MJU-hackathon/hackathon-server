package sopt.thingkerton.domain.quest.service;

import java.util.List;
import sopt.thingkerton.domain.profile.entity.Interest;
import sopt.thingkerton.domain.quest.entity.PlaceActivityKey;

/**
 * 생성기가 만든 퀘스트 1개. 문구는 요청 언어로 작성돼 있다.
 */
public record GeneratedQuest(
        PlaceActivityKey key,
        Interest category,
        int estimatedMinutes,
        String placeName,
        String title,
        String reason,
        String description,
        List<String> steps
) {
}
