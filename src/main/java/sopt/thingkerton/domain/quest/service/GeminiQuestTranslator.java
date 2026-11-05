package sopt.thingkerton.domain.quest.service;

import java.util.List;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import sopt.thingkerton.domain.profile.entity.AppLocale;
import sopt.thingkerton.domain.quest.entity.QuestText;
import sopt.thingkerton.global.infra.ai.AiClient;
import tools.jackson.databind.json.JsonMapper;

/**
 * 퀘스트 설명을 한 번의 Gemini 호출로 번역한다.
 */
@Component
@RequiredArgsConstructor
public class GeminiQuestTranslator implements QuestTranslator {

    private static final String SYSTEM_PROMPT = """
            You translate short quest descriptions for an app used by international students in Seoul.
            Keep the meaning, place names, and number of steps. Do not add new information.
            """;

    private static final JsonMapper MAPPER = JsonMapper.builder().build();

    private final AiClient aiClient;

    @Override
    public List<TranslatedText> translate(List<QuestText> sources, AppLocale target) {
        List<Item> items = sources.stream()
                .map(text -> new Item(text.getTitle(), text.getReason(), text.getDescription(), text.getSteps()))
                .toList();
        String userPrompt = """
                Translate every item into %s (%s). Return the items in the same order.
                Items:
                %s
                """.formatted(target.getDisplayName(), target.getCode(), MAPPER.writeValueAsString(items));
        Payload payload = aiClient.chat(SYSTEM_PROMPT, userPrompt, Payload.class);
        if (payload == null || payload.items() == null || payload.items().size() != sources.size()) {
            throw new IllegalStateException("AI translation size mismatch");
        }
        return IntStream.range(0, sources.size())
                .mapToObj(i -> toText(payload.items().get(i), sources.get(i)))
                .toList();
    }

    /**
     * 비어 있는 필드는 원문으로 채운다.
     */
    private TranslatedText toText(Item item, QuestText source) {
        if (item == null) {
            throw new IllegalStateException("AI translation item is null");
        }
        List<String> steps = item.steps() == null || item.steps().isEmpty() ? source.getSteps() : item.steps();
        return new TranslatedText(
                orDefault(item.title(), source.getTitle()),
                orDefault(item.reason(), source.getReason()),
                orDefault(item.description(), source.getDescription()),
                steps);
    }

    private String orDefault(String value, String defaultValue) {
        return StringUtils.hasText(value) ? value.strip() : defaultValue;
    }

    record Payload(List<Item> items) {
    }

    record Item(String title, String reason, String description, List<String> steps) {
    }
}
