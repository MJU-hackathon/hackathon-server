package sopt.thingkerton.domain.diary.service;

import java.util.List;

/**
 * @param diaries 작성 시각 최신순
 */
public record DiaryListResult(
        List<LocalizedDiary> diaries,
        long experiencedCount,
        long notExperiencedCount
) {

    public long totalCount() {
        return diaries.size();
    }
}
