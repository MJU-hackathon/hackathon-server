package sopt.thingkerton.support;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import sopt.thingkerton.domain.profile.entity.AppLocale;
import sopt.thingkerton.domain.quest.entity.QuestText;
import sopt.thingkerton.domain.quest.service.QuestTranslator;
import sopt.thingkerton.domain.quest.service.TranslatedText;

/**
 * 문구 앞의 "[언어]" 표시만 바꾸는 가짜 번역기.
 */
public class FakeQuestTranslator implements QuestTranslator {

    private final AtomicInteger calls = new AtomicInteger();

    @Override
    public List<TranslatedText> translate(List<QuestText> sources, AppLocale target) {
        calls.incrementAndGet();
        return sources.stream()
                .map(source -> new TranslatedText(
                        relabel(source.getTitle(), target),
                        relabel(source.getReason(), target),
                        relabel(source.getDescription(), target),
                        source.getSteps().stream().map(step -> relabel(step, target)).toList()))
                .toList();
    }

    public int calls() {
        return calls.get();
    }

    public void reset() {
        calls.set(0);
    }

    private String relabel(String text, AppLocale target) {
        return text.replaceFirst("^\\[[a-z]{2}]", "[" + target.getCode() + "]");
    }
}
