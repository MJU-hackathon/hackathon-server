package sopt.thingkerton.domain.diary.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import sopt.thingkerton.domain.diary.service.DiaryListResult;

/**
 * 일기 목록(WF-05). 월별로 묶고, 묶음과 항목 모두 최신순이다.
 */
@Schema(description = "나의 유학 일기 목록 (WF-05)")
public record DiaryListResponse(
        @Schema(description = "전체 기록 수. 0이면 WF-05b 빈 화면", example = "2")
        long totalCount,

        @Schema(description = "해봤어요! 개수", example = "1")
        long experiencedCount,

        @Schema(description = "이번엔 못 했어요 개수", example = "1")
        long notExperiencedCount,

        @Schema(description = "월별 묶음 (최신 월 먼저)")
        List<MonthGroup> groups
) {

    public static DiaryListResponse from(DiaryListResult result) {
        Map<YearMonth, List<DiaryResponse>> grouped = result.diaries().stream()
                .map(DiaryResponse::from)
                .collect(Collectors.groupingBy(
                        diary -> YearMonth.from(diary.recordedAt()),
                        LinkedHashMap::new,
                        Collectors.toList()));
        List<MonthGroup> groups = grouped.entrySet().stream()
                .map(entry -> new MonthGroup(entry.getKey().toString(), entry.getValue()))
                .toList();
        return new DiaryListResponse(result.totalCount(), result.experiencedCount(), result.notExperiencedCount(),
                groups);
    }

    /**
     * @param yearMonth YYYY-MM (Asia/Seoul)
     */
    @Schema(description = "한 달 치 일기")
    public record MonthGroup(
            @Schema(description = "연월 YYYY-MM (Asia/Seoul)", example = "2026-11")
            String yearMonth,

            @Schema(description = "그 달의 일기, 작성 시각 최신순")
            List<DiaryResponse> diaries
    ) {
    }
}
