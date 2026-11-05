package sopt.thingkerton.domain.quest.entity;

/**
 * 퀘스트 상태. 모든 퀘스트는 AVAILABLE로 시작한다.
 * <p>
 * EXPIRED는 DB에 저장하지 않고, 묶음 날짜가 오늘(Asia/Seoul)보다 이전인 AVAILABLE 퀘스트를 조회 시점에 만료로 판정한다.
 */
public enum QuestStatus {
    AVAILABLE, SKIPPED, RECORDED, EXPIRED
}
