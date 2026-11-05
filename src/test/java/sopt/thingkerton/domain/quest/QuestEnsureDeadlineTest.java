package sopt.thingkerton.domain.quest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import sopt.thingkerton.support.FakeAiConfig;
import sopt.thingkerton.support.FakeGeocodingPort;
import sopt.thingkerton.support.FakeQuestGenerator;
import sopt.thingkerton.support.FakeQuestImageGenerator;
import sopt.thingkerton.support.FixedClockConfig;
import sopt.thingkerton.support.MutableClock;

/**
 * 묶음 생성 전체 마감(ensure-deadline): 퀘스트 생성 후 남은 시간을 대표 이미지 생성에 쓴다.
 * 이미지 상한(5초)을 마감(2초)보다 크게 두어, 이미지 제한 시간이 남은 시간으로 정해지는지 확인한다.
 */
@SpringBootTest(properties = {
        "app.quest.ensure-deadline=2s",
        "app.quest.image-generation-timeout=5s"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({FixedClockConfig.class, FakeAiConfig.class})
class QuestEnsureDeadlineTest {

    private static final String DEFAULT_FOOD_IMAGE = "https://cdn.example.com/quest-images/food.jpg";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private FakeQuestGenerator generator;
    @Autowired
    private FakeQuestImageGenerator imageGenerator;
    @Autowired
    private FakeGeocodingPort geocodingPort;
    @Autowired
    private MutableClock clock;

    @AfterEach
    void tearDown() {
        generator.reset();
        imageGenerator.reset();
        geocodingPort.reset();
        clock.reset();
    }

    @Test
    void 퀘스트_생성이_빨리_끝나면_남은_시간을_이미지_생성에_쓴다() throws Exception {
        // 고정 1초 제한이었다면 1.5초 걸리는 이미지는 실패하지만, 마감까지 2초가 남아 성공한다
        imageGenerator.setDelayMillis(1_500);

        List<String> imageUrls = imageUrls(ensure(newUserWithProfile()));

        assertThat(imageUrls).allSatisfy(url -> assertThat(url).contains("/quest-images/place-"));
    }

    @Test
    void 퀘스트_생성이_오래_걸리면_마감까지_남은_시간만_이미지에_쓰고_기본_이미지로_저장한다() throws Exception {
        // 생성 0.9초 → 남은 시간 약 1.1초 < 이미지 1.5초. 이미지 상한(5초)까지 기다리지 않는다
        generator.setDelayMillis(900);
        imageGenerator.setDelayMillis(1_500);

        long start = System.currentTimeMillis();
        List<String> imageUrls = imageUrls(ensure(newUserWithProfile()));
        long elapsed = System.currentTimeMillis() - start;

        assertThat(imageUrls.getFirst()).isEqualTo(DEFAULT_FOOD_IMAGE);
        assertThat(elapsed).isLessThan(2_400);
    }

    private List<String> imageUrls(String body) {
        return JsonPath.read(body, "$.data[*].imageUrl");
    }

    private String newUserWithProfile() throws Exception {
        String userKey = UUID.randomUUID().toString();
        mockMvc.perform(put("/api/profile")
                        .header("X-User-Key", userKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nickname":"하나","locale":"ko","birthDate":"2003-04-12",
                                 "stayStartDate":"2026-03-01","stayEndDate":"2027-02-28","interests":["KPOP","FOOD"]}
                                """))
                .andExpect(status().isOk());
        return userKey;
    }

    private String ensure(String userKey) throws Exception {
        return mockMvc.perform(post("/api/daily-quests/ensure")
                        .header("X-User-Key", userKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"latitude\":37.5718,\"longitude\":126.9620}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
    }
}
