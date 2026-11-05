package sopt.thingkerton.domain.diary.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import sopt.thingkerton.domain.diary.entity.Diary;
import sopt.thingkerton.domain.diary.entity.Outcome;
import sopt.thingkerton.domain.diary.service.LocalizedDiary;
import sopt.thingkerton.global.time.SeoulTime;

/**
 * 일기 1개. 저장 응답과 목록 항목이 같은 형태다.
 */
@Schema(description = "일기 1개 (저장 응답·목록 항목 공통)")
public record DiaryResponse(
        @Schema(description = "일기 ID", example = "31")
        Long diaryId,

        @Schema(description = "기록한 퀘스트 ID", example = "2")
        Long questId,

        @Schema(description = "퀘스트 제목 (목록은 요청 언어, 저장 응답은 프로필 언어)", example = "영천시장 수제 꽈배기 맛보기")
        String title,

        @Schema(description = "작성 당시 장소명 (번역하지 않음)", example = "영천시장")
        String placeName,

        @Schema(description = "경험 사진 URL (S3)",
                example = "https://mju-hackathon-photos-2026.s3.ap-southeast-2.amazonaws.com/diaries/u1/31.jpg")
        String photoUrl,

        @Schema(description = "경험 결과", example = "EXPERIENCED", allowableValues = {"EXPERIENCED", "NOT_EXPERIENCED"})
        Outcome outcome,

        @Schema(description = "한 줄 메모 원문. 없으면 null", example = "처음 먹어본 호떡, 생각보다 달았다.", nullable = true)
        String memo,

        @Schema(description = "저장 시각 (Asia/Seoul, +09:00)", example = "2026-11-06T18:20:00+09:00")
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
