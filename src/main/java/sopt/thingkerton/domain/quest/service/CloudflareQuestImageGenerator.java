package sopt.thingkerton.domain.quest.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import sopt.thingkerton.global.infra.ai.AiGeneratedImage;
import sopt.thingkerton.global.infra.ai.CloudflareImageClient;
import sopt.thingkerton.global.infra.s3.S3Uploader;

/**
 * Cloudflare Workers AI(FLUX)로 퀘스트 카드 대표 이미지를 만들어 S3({@value QuestImageResolver#GENERATED_DIR}/)에 저장한다.
 * <p>
 * FLUX는 영어 프롬프트에 맞춰져 있어 사용자 언어 문구(장소명·제목) 대신 영어 slug인 placeKey·activityKey로 프롬프트를 만든다.
 * 실제 호출로 확인한 FLUX schnell 특성 (2026-11-06):
 * <ul>
 *   <li>부정 지시("글자 넣지 마")는 무시되고 오히려 간판 글자를 그린다 → "blank signboards, wordless" 긍정형으로 적는다</li>
 *   <li>"Theme: food" 같은 라벨 형식 문구는 간판 글자로 그대로 그린다 → 라벨 없이 문장으로 쓴다</li>
 *   <li>"Korean"을 넣으면 가짜 한글 간판이 늘어난다 → 지역은 "Seodaemun-gu, Seoul"로만 표현한다</li>
 * </ul>
 * 그래도 가게·시장 장면에서는 가짜 글자가 가끔 나온다(모델 한계).
 */
@Component
@RequiredArgsConstructor
public class CloudflareQuestImageGenerator implements QuestImageGenerator {

    private static final String PROMPT_TEMPLATE = "Flat vector illustration for a mobile app card. "
            + "%s at %s in Seodaemun-gu, Seoul, South Korea. "
            + "Soft pastel colors, bright and friendly mood, simple shapes, clean composition. "
            + "Plain blank signboards, wordless, minimalist graphic style. Small people in the background.";

    private final CloudflareImageClient cloudflareImageClient;
    private final S3Uploader s3Uploader;

    @Override
    public String generate(GeneratedQuest quest) {
        AiGeneratedImage image = cloudflareImageClient.generate(prompt(quest));
        return s3Uploader.uploadImage(image.data(), image.contentType(), QuestImageResolver.GENERATED_DIR).key();
    }

    static String prompt(GeneratedQuest quest) {
        return PROMPT_TEMPLATE.formatted(
                capitalize(words(quest.key().activityKey())),
                words(quest.key().placeKey()));
    }

    private static String words(String slug) {
        return slug.replace('-', ' ');
    }

    private static String capitalize(String value) {
        return value.isEmpty() ? value : Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }
}
