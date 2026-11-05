package sopt.thingkerton.domain.diary.dto;

import java.time.OffsetDateTime;
import sopt.thingkerton.domain.diary.entity.Diary;
import sopt.thingkerton.domain.diary.entity.Outcome;
import sopt.thingkerton.domain.diary.service.LocalizedDiary;
import sopt.thingkerton.global.time.SeoulTime;

/**
 * 일기 1개. 저장 응답과 목록 항목이 같은 형태다.
 */
public record DiaryResponse(
        Long diaryId,
        Long questId,
        String title,
        String placeName,
        String photoUrl,
        Outcome outcome,
        String memo,
        OffsetDateTime recordedAt
) {

    public static DiaryResponse from(LocalizedDiary localized) {
        Diary diary = localized.diary();
        return new DiaryResponse(
                diary.getId(),
                diary.getQuest().getId(),
                localized.title(),
                diary.getPlaceSnapshot(),
                localized.photoUrl(),
                diary.getOutcome(),
                diary.getMemo(),
                diary.getCreatedAt().atZone(SeoulTime.ZONE).toOffsetDateTime());
    }
}
