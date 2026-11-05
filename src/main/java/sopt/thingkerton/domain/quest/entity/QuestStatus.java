package sopt.thingkerton.domain.quest.entity;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 퀘스트 상태. 모든 퀘스트는 AVAILABLE로 시작한다.
 * <p>
 * EXPIRED는 DB에 저장하지 않고, 묶음 날짜가 오늘(Asia/Seoul)보다 이전인 AVAILABLE 퀘스트를 조회 시점에 만료로 판정한다.
 */
@Schema(description = "퀘스트 상태. AVAILABLE=진행 가능, SKIPPED=패스함, RECORDED=일기 저장함, EXPIRED=지난 날 퀘스트")
public enum QuestStatus {
    AVAILABLE, SKIPPED, RECORDED, EXPIRED
}
