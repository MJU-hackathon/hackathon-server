package sopt.thingkerton.domain.quest.service;

import java.util.List;

/**
 * 퀘스트 생성기. 테스트에서는 가짜 구현으로 교체한다.
 * <p>
 * 정확히 {@link #QUEST_COUNT}개를 반환하지 못하면 예외를 던진다. 호출 측이 데모 퀘스트로 대체한다.
 */
public interface QuestGenerator {

    int QUEST_COUNT = 3;

    List<GeneratedQuest> generate(QuestGenerationCommand command);
}
