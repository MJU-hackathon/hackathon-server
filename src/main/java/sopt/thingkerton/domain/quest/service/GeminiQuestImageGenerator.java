package sopt.thingkerton.domain.quest.service;

import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import sopt.thingkerton.global.infra.ai.AiGeneratedImage;
import sopt.thingkerton.global.infra.ai.AiImageClient;
import sopt.thingkerton.global.infra.s3.S3Uploader;

/**
 * Gemini로 퀘스트 카드 대표 이미지를 만들어 S3({@value QuestImageResolver#GENERATED_DIR}/)에 저장한다.
 */
@Component
@RequiredArgsConstructor
public class GeminiQuestImageGenerator implements QuestImageGenerator {

    private static final String PROMPT_TEMPLATE = """
            Create a warm, friendly illustration for a mobile app card about a neighborhood activity \
            in Seodaemun-gu, Seoul, South Korea.

            Place: %s
            Category: %s
            Activity: %s
            Quest: %s

            Style: soft, bright flat illustration with gentle colors, showing the place and the activity.
            Do not include any text, letters, logos, signs with writing, or watermarks.
            People may appear small in the scene, but no close-up faces.
            """;

    private final AiImageClient aiImageClient;
    private final S3Uploader s3Uploader;

    @Override
    public String generate(GeneratedQuest quest) {
        AiGeneratedImage image = aiImageClient.generate(prompt(quest));
        return s3Uploader.uploadImage(image.data(), image.contentType(), QuestImageResolver.GENERATED_DIR).key();
    }

    static String prompt(GeneratedQuest quest) {
        return PROMPT_TEMPLATE.formatted(
                quest.placeName(),
                quest.category().name().toLowerCase(Locale.ROOT),
                quest.key().activityKey().replace('-', ' '),
                quest.title());
    }
}
