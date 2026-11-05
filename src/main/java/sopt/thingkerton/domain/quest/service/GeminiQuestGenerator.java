package sopt.thingkerton.domain.quest.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import sopt.thingkerton.domain.profile.entity.Interest;
import sopt.thingkerton.domain.quest.entity.PlaceActivityKey;
import sopt.thingkerton.global.infra.ai.AiClient;

/**
 * Gemini로 퀘스트를 생성한다. 장소는 좌표 기준 반경 3km 안으로 요구하지만 실존·거리 검증은 하지 않는다.
 */
@Component
@RequiredArgsConstructor
public class GeminiQuestGenerator implements QuestGenerator {

    private static final int MIN_MINUTES = 10;
    private static final int MAX_MINUTES = 240;

    private static final String SYSTEM_PROMPT = """
            You plan small neighborhood quests in Seoul for international students.
            Each quest is one concrete activity at one specific real place the student can visit today.
            Write every user-facing text in the requested output language. Keep placeKey and activityKey in English.
            """;

    private final AiClient aiClient;

    @Override
    public List<GeneratedQuest> generate(QuestGenerationCommand command) {
        Payload payload = aiClient.chat(SYSTEM_PROMPT, userPrompt(command), Payload.class);
        List<GeneratedQuest> quests = sanitize(payload, command.excludedKeys());
        if (quests.size() < QUEST_COUNT) {
            throw new IllegalStateException("AI returned " + quests.size() + " valid quests");
        }
        return quests.subList(0, QUEST_COUNT);
    }

    private String userPrompt(QuestGenerationCommand command) {
        String interests = command.interests().stream().map(Enum::name).collect(Collectors.joining(", "));
        String categories = Arrays.stream(Interest.values()).map(Enum::name).collect(Collectors.joining(", "));
        String excluded = command.excludedKeys().isEmpty()
                ? "(none)"
                : command.excludedKeys().stream()
                        .map(key -> key.placeKey() + " + " + key.activityKey())
                        .collect(Collectors.joining("\n- ", "- ", ""));
        return """
                Create exactly %d quests.

                Location: latitude %s, longitude %s. Every place must be within a 3km radius of this point.
                User interests: %s.
                Reflect the interests first, but mix in other categories: about 2 of the 3 quests from the interests and 1 from a different category.
                Allowed categories: %s.
                Do not use these place + activity combinations the user already experienced (same place with a different activity is fine):
                %s

                Rules for each quest:
                - placeKey, activityKey: short English lowercase slugs with hyphens (e.g. "yeongcheon-market", "order-market-food")
                - category: one of the allowed categories
                - estimatedMinutes: integer between %d and %d
                - placeName: the place's common name
                - address: the place's Korean road-name address in Korean (e.g. "서울특별시 서대문구 통일로 247"); empty string if you are not sure
                - title: short catchy title, max 30 characters
                - reason: one line explaining why this was recommended based on the interests, max 60 characters
                - description: 1-2 sentences introducing the activity
                - steps: exactly 3 short action steps; the last step is taking a photo
                - All quests must use different places.

                Output language: %s (%s).
                """.formatted(QUEST_COUNT, command.latitude().toPlainString(), command.longitude().toPlainString(),
                interests, categories, excluded, MIN_MINUTES, MAX_MINUTES,
                command.locale().getDisplayName(), command.locale().getCode());
    }

    /**
     * 형식이 잘못됐거나 제외 대상·중복인 퀘스트는 버린다.
     */
    private List<GeneratedQuest> sanitize(Payload payload, Set<PlaceActivityKey> excludedKeys) {
        List<GeneratedQuest> result = new ArrayList<>();
        if (payload == null || payload.quests() == null) {
            return result;
        }
        Set<PlaceActivityKey> seen = new HashSet<>(excludedKeys);
        for (Item item : payload.quests()) {
            if (item == null) {
                continue;
            }
            PlaceActivityKey key = new PlaceActivityKey(item.placeKey(), item.activityKey());
            Interest category = parseCategory(item.category());
            List<String> steps = item.steps() == null ? List.of()
                    : item.steps().stream().filter(StringUtils::hasText).map(String::strip).toList();
            if (key.placeKey().isEmpty() || key.activityKey().isEmpty() || category == null
                    || !StringUtils.hasText(item.placeName()) || !StringUtils.hasText(item.title())
                    || steps.isEmpty() || !seen.add(key)) {
                continue;
            }
            result.add(new GeneratedQuest(
                    key,
                    category,
                    Math.clamp(item.estimatedMinutes() == null ? 30 : item.estimatedMinutes(), MIN_MINUTES, MAX_MINUTES),
                    truncate(item.placeName(), 100),
                    StringUtils.hasText(item.address()) ? truncate(item.address(), 255) : null,
                    null,
                    truncate(item.title(), 100),
                    truncate(StringUtils.hasText(item.reason()) ? item.reason() : "", 255),
                    StringUtils.hasText(item.description()) ? item.description().strip() : "",
                    steps));
        }
        return result;
    }

    private Interest parseCategory(String category) {
        if (category == null) {
            return null;
        }
        return Arrays.stream(Interest.values())
                .filter(value -> value.name().equalsIgnoreCase(category.strip()))
                .findFirst()
                .orElse(null);
    }

    private String truncate(String value, int maxLength) {
        String stripped = value.strip();
        return stripped.length() <= maxLength ? stripped : stripped.substring(0, maxLength);
    }

    record Payload(List<Item> quests) {
    }

    record Item(
            String placeKey,
            String activityKey,
            String category,
            Integer estimatedMinutes,
            String placeName,
            String address,
            String title,
            String reason,
            String description,
            List<String> steps
    ) {
    }
}
