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
 * @param imageRef   AI로 생성해 저장한 대표 이미지 참조(S3 키). 아직 생성 전이거나 실패하면 null (카테고리 기본 이미지 사용)
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
        List<String> steps,
        String imageRef
) {

    /**
     * 생성기·데모용. 대표 이미지는 이후 {@link QuestImageAttacher}가 채운다.
     */
    public GeneratedQuest(PlaceActivityKey key, Interest category, int estimatedMinutes, String placeName,
                          String address, Coordinate coordinate, String title, String reason, String description,
                          List<String> steps) {
        this(key, category, estimatedMinutes, placeName, address, coordinate, title, reason, description, steps,
                null);
    }

    public GeneratedQuest withCoordinate(Coordinate coordinate) {
        return new GeneratedQuest(key, category, estimatedMinutes, placeName, address, coordinate, title, reason,
                description, steps, imageRef);
    }

    public GeneratedQuest withImageRef(String imageRef) {
        return new GeneratedQuest(key, category, estimatedMinutes, placeName, address, coordinate, title, reason,
                description, steps, imageRef);
    }
}
