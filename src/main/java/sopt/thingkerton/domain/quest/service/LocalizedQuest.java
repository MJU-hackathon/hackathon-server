package sopt.thingkerton.domain.quest.service;

import sopt.thingkerton.domain.quest.entity.Quest;
import sopt.thingkerton.domain.quest.entity.QuestStatus;
import sopt.thingkerton.domain.quest.entity.QuestText;

/**
 * 요청 언어의 설명을 붙인 퀘스트. 번역에 실패하면 text는 원문 언어일 수 있다.
 *
 * @param status 오늘(Asia/Seoul) 기준 상태 (지난 묶음의 AVAILABLE은 EXPIRED)
 */
public record LocalizedQuest(
        Quest quest,
        QuestText text,
        String imageUrl,
        QuestStatus status
) {
}
