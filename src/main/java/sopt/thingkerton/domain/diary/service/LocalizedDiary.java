package sopt.thingkerton.domain.diary.service;

import sopt.thingkerton.domain.diary.entity.Diary;

/**
 * @param title 요청 언어의 퀘스트 제목 (없으면 작성 시점 제목)
 */
public record LocalizedDiary(
        Diary diary,
        String title,
        String photoUrl
) {
}
