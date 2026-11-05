package sopt.thingkerton.domain.diary.dto;

import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import sopt.thingkerton.domain.diary.service.DiaryListResult;

/**
 * 일기 목록(WF-05). 월별로 묶고, 묶음과 항목 모두 최신순이다.
 */
public record DiaryListResponse(
        long totalCount,
        long experiencedCount,
        long notExperiencedCount,
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
    public record MonthGroup(String yearMonth, List<DiaryResponse> diaries) {
    }
}
