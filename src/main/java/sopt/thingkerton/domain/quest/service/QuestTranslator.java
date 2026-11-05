package sopt.thingkerton.domain.quest.service;

import java.util.List;
import sopt.thingkerton.domain.profile.entity.AppLocale;
import sopt.thingkerton.domain.quest.entity.QuestText;

/**
 * 퀘스트 설명 번역기. 장소·활동은 그대로 두고 표현만 바꾼다. 테스트에서는 가짜 구현으로 교체한다.
 */
public interface QuestTranslator {

    /**
     * @return sources와 같은 순서·개수의 번역 결과. 실패하면 예외
     */
    List<TranslatedText> translate(List<QuestText> sources, AppLocale target);
}
