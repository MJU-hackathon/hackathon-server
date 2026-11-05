package sopt.thingkerton.domain.quest.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import sopt.thingkerton.domain.profile.entity.Interest;
import sopt.thingkerton.domain.quest.entity.PlaceActivityKey;
import sopt.thingkerton.global.infra.ai.AiGeneratedImage;
import sopt.thingkerton.global.infra.ai.CloudflareImageClient;
import sopt.thingkerton.global.infra.s3.S3Uploader;
import sopt.thingkerton.global.infra.s3.UploadedFile;

class CloudflareQuestImageGeneratorTest {

    private final CloudflareImageClient client = mock(CloudflareImageClient.class);
    private final S3Uploader s3Uploader = mock(S3Uploader.class);
    private final CloudflareQuestImageGenerator generator = new CloudflareQuestImageGenerator(client, s3Uploader);

    private final GeneratedQuest quest = new GeneratedQuest(
            new PlaceActivityKey("yeongcheon-market", "order-market-food"), Interest.FOOD, 40, "영천시장",
            "서울특별시 서대문구 통일로 189-1", null, "시장에서 만나는 첫 번째 한입", "reason", "description",
            List.of("step"));

    @Test
    void 영어_slug로_프롬프트를_만들고_사용자_언어_문구는_넣지_않는다() {
        String prompt = CloudflareQuestImageGenerator.prompt(quest);

        assertThat(prompt)
                .contains("Order market food at yeongcheon market in Seodaemun-gu, Seoul", "blank signboards, wordless")
                // 라벨 형식·"Korean"·부정 지시는 이미지에 글자를 늘린다
                .doesNotContain("영천시장", "시장에서", "Theme:", "Korean", "text", "letters");
    }

    @Test
    void 생성한_이미지를_quest_images에_올리고_S3_키를_반환한다() {
        byte[] data = {1, 2, 3};
        when(client.generate(anyString())).thenReturn(new AiGeneratedImage(data, "image/jpeg"));
        when(s3Uploader.uploadImage(data, "image/jpeg", "quest-images"))
                .thenReturn(new UploadedFile("quest-images/abc.jpg", "https://bucket/quest-images/abc.jpg"));

        assertThat(generator.generate(quest)).isEqualTo("quest-images/abc.jpg");
    }
}
