package sopt.thingkerton.domain.quest.service;

import java.util.List;
import sopt.thingkerton.domain.location.model.Coordinate;
import sopt.thingkerton.domain.profile.entity.Interest;
import sopt.thingkerton.domain.quest.entity.PlaceActivityKey;

/**
 * 생성기가 만든 퀘스트 1개. 문구는 요청 언어로 작성돼 있다.
 *
 * @param address    장소의 도로명 주소. 모르면 null
 * @param coordinate 주소를 좌표로 변환한 값. 아직 변환 전이거나 실패하면 null
 */
public record GeneratedQuest(
        PlaceActivityKey key,
        Interest category,
        int estimatedMinutes,
        String placeName,
        String address,
        Coordinate coordinate,
        String title,
        String reason,
        String description,
        List<String> steps
) {

    public GeneratedQuest withCoordinate(Coordinate coordinate) {
        return new GeneratedQuest(key, category, estimatedMinutes, placeName, address, coordinate, title, reason,
                description, steps);
    }
}
