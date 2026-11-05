package sopt.thingkerton.domain.quest.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import sopt.thingkerton.domain.profile.entity.AppLocale;
import sopt.thingkerton.domain.quest.entity.Quest;
import sopt.thingkerton.domain.quest.entity.QuestText;
import sopt.thingkerton.domain.quest.repository.QuestTextRepository;
import sopt.thingkerton.global.infra.ai.AiTimeLimiter;

/**
 * 퀘스트 설명을 요청 언어로 제공한다. 없으면 기존 설명을 번역해 캐시한다.
 * 번역이 실패하거나 제한 시간을 넘기면 원문 설명을 반환하고 캐시하지 않는다(다음 요청에서 다시 번역).
 * <p>
 * 번역(LLM 호출)을 트랜잭션 밖에서 하기 위해 클래스에 트랜잭션을 걸지 않는다. 저장은 Repository 트랜잭션으로 처리한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QuestTextService {

    private final QuestTextRepository questTextRepository;
    private final QuestTranslator questTranslator;
    private final AiTimeLimiter aiTimeLimiter;
    private final QuestProperties questProperties;

    /**
     * @return questId → 요청 언어 설명. 번역에 실패한 퀘스트는 원문 설명
     */
    public Map<Long, QuestText> getTexts(List<Quest> quests, AppLocale locale) {
        if (quests.isEmpty()) {
            return Map.of();
        }
        Map<Long, List<QuestText>> textsByQuest = questTextRepository
                .findAllByQuestIdIn(quests.stream().map(Quest::getId).toList())
                .stream()
                .collect(Collectors.groupingBy(QuestText::getQuestId));

        Map<Long, QuestText> result = new LinkedHashMap<>();
        List<QuestText> sources = new ArrayList<>();
        for (Quest quest : quests) {
            List<QuestText> texts = textsByQuest.getOrDefault(quest.getId(), List.of());
            texts.stream()
                    .filter(text -> text.getAppLocale() == locale)
                    .findFirst()
                    .ifPresentOrElse(
                            text -> result.put(quest.getId(), text),
                            () -> texts.stream().findFirst().ifPresent(sources::add));
        }
        if (!sources.isEmpty()) {
            translate(sources, locale).forEach(text -> result.put(text.getQuestId(), text));
        }
        return result;
    }

    private List<QuestText> translate(List<QuestText> sources, AppLocale locale) {
        List<TranslatedText> translated;
        try {
            translated = aiTimeLimiter.call(() -> questTranslator.translate(sources, locale),
                    questProperties.translationTimeout());
        } catch (RuntimeException exception) {
            log.warn("Quest translation failed. locale={}, questIds={}", locale.getCode(),
                    sources.stream().map(QuestText::getQuestId).toList(), exception);
            return sources;
        }
        List<QuestText> texts = new ArrayList<>();
        for (int i = 0; i < sources.size(); i++) {
            TranslatedText text = translated.get(i);
            texts.add(QuestText.create(sources.get(i).getQuestId(), locale, text.title(), text.reason(),
                    text.description(), text.steps()));
        }
        try {
            questTextRepository.saveAll(texts);
        } catch (DataIntegrityViolationException exception) {
            // 같은 언어 번역이 동시에 저장된 경우. 이번 응답은 방금 번역한 값을 그대로 쓴다.
            log.info("Quest translation already cached. locale={}", locale.getCode());
        }
        return texts;
    }
}
