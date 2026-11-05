package sopt.thingkerton.domain.quest.service;

/**
 * 퀘스트 카드 대표 이미지 생성기. 이미지를 만들어 저장하고 참조를 돌려준다. 테스트에서는 가짜 구현으로 교체한다.
 * <p>
 * 실패하면 예외를 던진다. 호출 측({@link QuestImageAttacher})이 카테고리 기본 이미지로 대체한다.
 */
public interface QuestImageGenerator {

    /**
     * @return 저장된 이미지 참조. {@link QuestImageResolver#isGenerated(String)}가 true인 값이어야 한다
     */
    String generate(GeneratedQuest quest);
}
