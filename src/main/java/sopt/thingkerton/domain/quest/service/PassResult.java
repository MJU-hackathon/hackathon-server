package sopt.thingkerton.domain.quest.service;

import sopt.thingkerton.domain.quest.entity.Quest;

/**
 * @param remainingCount 패스 후 같은 묶음에 남은 AVAILABLE 퀘스트 수
 */
public record PassResult(
        Quest quest,
        long remainingCount
) {
}
